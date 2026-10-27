param([string]$PostgresBin='C:\Program Files\PostgreSQL\18\bin',[switch]$Offline)
$ErrorActionPreference='Stop'
$qaRepo=Split-Path $PSScriptRoot -Parent
$qaRoot=Join-Path $qaRepo ('.tmp\qa-'+[guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force $qaRoot | Out-Null
$qaData=Join-Path $qaRoot data
$qaSocket=[Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback,0)
$qaSocket.Start();$qaPort=$qaSocket.LocalEndpoint.Port;$qaSocket.Stop()
$qaOldUrl=$env:GREENOCEAN_TEST_DB_URL
try {
  & (Join-Path $PostgresBin initdb.exe) -D $qaData -U postgres -A trust -E UTF8 --no-sync *> (Join-Path $qaRoot init.log)
  if($LASTEXITCODE -ne 0){throw 'Temporary database initialization failed; see init.log'}
  $qaServer=Start-Process -FilePath (Join-Path $PostgresBin postgres.exe) -ArgumentList @('-D',('"'+$qaData+'"'),'-h','127.0.0.1','-p',$qaPort,'-c','max_connections=40','-c','fsync=off') -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $qaRoot postgres.log) -RedirectStandardError (Join-Path $qaRoot postgres-error.log)
  $qaReady=$false
  for($qaTry=0;$qaTry -lt 60;$qaTry++) {
    & (Join-Path $PostgresBin pg_isready.exe) -h 127.0.0.1 -p $qaPort -U postgres *> $null
    if($LASTEXITCODE -eq 0){$qaReady=$true;break};Start-Sleep -Milliseconds 250
  }
  if(!$qaReady){throw 'Temporary database did not become ready'}
  $env:GREENOCEAN_TEST_DB_URL="jdbc:postgresql://127.0.0.1:$qaPort/postgres"
  $qaArgs=@('-B','-f',(Join-Path $qaRepo backend/pom.xml),'clean','verify')
  if($Offline){$qaArgs=@('-o')+$qaArgs}
  & (Join-Path $qaRepo backend/mvnw.cmd) @qaArgs
  if($LASTEXITCODE -ne 0){throw 'Backend verification failed'}
  Push-Location (Join-Path $qaRepo frontend)
  try {npm test;if($LASTEXITCODE -ne 0){throw 'Frontend tests failed'}} finally {Pop-Location}
  Write-Output "QA data and logs: $qaRoot"
} finally {
  $env:GREENOCEAN_TEST_DB_URL=$qaOldUrl
  if($qaServer -and !$qaServer.HasExited){& (Join-Path $PostgresBin pg_ctl.exe) -D $qaData -m fast -w stop *> (Join-Path $qaRoot stop.log)}
}
