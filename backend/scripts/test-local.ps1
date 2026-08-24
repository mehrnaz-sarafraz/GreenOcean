$ErrorActionPreference = 'Stop'

$hadExistingPassword = Test-Path Env:GREENOCEAN_DB_PASSWORD
$existingPassword = $env:GREENOCEAN_DB_PASSWORD
$hadExistingPgPassword = Test-Path Env:PGPASSWORD
$existingPgPassword = $env:PGPASSWORD
$securePassword = Read-Host -Prompt 'PostgreSQL password for user postgres' -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
$testExitCode = 1

try {
    $env:GREENOCEAN_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    $env:PGPASSWORD = $env:GREENOCEAN_DB_PASSWORD
    & psql --host localhost --port 5432 --username postgres --dbname greenocean --no-password --tuples-only --command 'SELECT 1;' | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw 'PostgreSQL authentication failed. Enter the current password for user postgres.'
    }
    & (Join-Path $PSScriptRoot '..\mvnw.cmd') clean test
    $testExitCode = $LASTEXITCODE
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    if ($hadExistingPassword) {
        $env:GREENOCEAN_DB_PASSWORD = $existingPassword
    }
    else {
        Remove-Item Env:GREENOCEAN_DB_PASSWORD -ErrorAction SilentlyContinue
    }
    if ($hadExistingPgPassword) {
        $env:PGPASSWORD = $existingPgPassword
    }
    else {
        Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    }
}

if ($testExitCode -ne 0) {
    Write-Error "Backend tests failed with exit code $testExitCode"
}
