$ErrorActionPreference = 'Stop'

Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    $embeddedTemp = Join-Path (Get-Location) 'target\embedded-temp'
    New-Item -ItemType Directory -Force -Path $embeddedTemp | Out-Null
    $env:TEMP = $embeddedTemp
    $env:TMP = $embeddedTemp
    Write-Host 'Starting GreenOcean with an embedded PostgreSQL development database...'
    Write-Host 'The database is temporary and is rebuilt from Flyway migrations on each run.'
    .\mvnw.cmd spring-boot:run '-Dspring-boot.run.main-class=com.greenocean.backend.local.LocalBackendApplication'
}
finally {
    Pop-Location
}
