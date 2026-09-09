param(
    [Parameter(Mandatory=$true)][string]$OutputFile,
    [string]$Container = "greenocean-database-1"
)
$ErrorActionPreference = "Stop"
$resolvedParent = [System.IO.Path]::GetFullPath((Split-Path -Parent $OutputFile))
New-Item -ItemType Directory -Force -Path $resolvedParent | Out-Null
docker exec $Container pg_dump --format=custom --no-owner --username=greenocean greenocean | Set-Content -AsByteStream -Path $OutputFile
Write-Host "Encrypted storage is recommended for backup file: $OutputFile"
