param(
    [Parameter(Mandatory=$true)][string]$InputFile,
    [string]$Container = "greenocean-database-1",
    [switch]$ConfirmRestore
)
$ErrorActionPreference = "Stop"
if (-not $ConfirmRestore) { throw "Restore replaces database objects. Re-run with -ConfirmRestore after validating the target." }
$resolved = (Resolve-Path -LiteralPath $InputFile).Path
Get-Content -AsByteStream -Raw -LiteralPath $resolved | docker exec -i $Container pg_restore --clean --if-exists --no-owner --username=greenocean --dbname=greenocean
