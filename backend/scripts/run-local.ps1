$ErrorActionPreference = 'Stop'

$hadExistingDbPassword = Test-Path Env:GREENOCEAN_DB_PASSWORD
$existingDbPassword = $env:GREENOCEAN_DB_PASSWORD
$hadExistingPgPassword = Test-Path Env:PGPASSWORD
$existingPgPassword = $env:PGPASSWORD
$hadExistingJwtSecret = Test-Path Env:GREENOCEAN_JWT_SECRET
$existingJwtSecret = $env:GREENOCEAN_JWT_SECRET

$securePassword = Read-Host -Prompt 'PostgreSQL password for user postgres' -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)

try {
    $env:GREENOCEAN_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    $env:PGPASSWORD = $env:GREENOCEAN_DB_PASSWORD

    & psql --host localhost --port 5432 --username postgres --dbname greenocean --no-password --tuples-only --command 'SELECT 1;' | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw 'PostgreSQL authentication failed. Enter the current password for user postgres.'
    }

    $env:GREENOCEAN_JWT_SECRET = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')
    & (Join-Path $PSScriptRoot '..\mvnw.cmd') spring-boot:run
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)

    if ($hadExistingDbPassword) {
        $env:GREENOCEAN_DB_PASSWORD = $existingDbPassword
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

    if ($hadExistingJwtSecret) {
        $env:GREENOCEAN_JWT_SECRET = $existingJwtSecret
    }
    else {
        Remove-Item Env:GREENOCEAN_JWT_SECRET -ErrorAction SilentlyContinue
    }
}
