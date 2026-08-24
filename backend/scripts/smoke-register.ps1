$runId = Get-Date -Format 'yyyyMMddHHmmss'
$endpoint = 'http:' + '//' + 'localhost:8080' + '/api/v1/auth/register'

$payload = @{
    email       = "demo-$runId@example.test"
    password    = 'StrongPassword_2026'
    username    = "demo$runId"
    displayName = 'Demo User'
    birthYear   = 2000
    countryCode = 'IR'
    city        = 'Tehran'
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri $endpoint -ContentType 'application/json' -Body $payload
