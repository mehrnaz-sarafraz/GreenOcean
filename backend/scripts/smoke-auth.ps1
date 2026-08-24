$ErrorActionPreference = 'Stop'

$runId = Get-Date -Format 'yyyyMMddHHmmss'
$baseUrl = 'http:' + '//' + 'localhost:8080' + '/api/v1/auth'
$email = "auth-$runId@example.test"
$password = 'StrongPassword_2026'

$registerPayload = @{
    email = $email
    password = $password
    username = "auth$runId"
    displayName = 'Auth Smoke User'
    birthYear = 2000
    countryCode = 'IR'
    city = 'Tehran'
} | ConvertTo-Json

$registered = Invoke-RestMethod -Method Post -Uri "$baseUrl/register" -ContentType 'application/json' -Body $registerPayload

$loginPayload = @{
    email = $email
    password = $password
    deviceName = 'PowerShell smoke test'
} | ConvertTo-Json

$tokens = Invoke-RestMethod -Method Post -Uri "$baseUrl/login" -ContentType 'application/json' -Body $loginPayload
$headers = @{ Authorization = "Bearer $($tokens.accessToken)" }
$currentUser = Invoke-RestMethod -Method Get -Uri "$baseUrl/me" -Headers $headers

$refreshPayload = @{ refreshToken = $tokens.refreshToken } | ConvertTo-Json
$rotatedTokens = Invoke-RestMethod -Method Post -Uri "$baseUrl/refresh" -ContentType 'application/json' -Body $refreshPayload

$logoutPayload = @{ refreshToken = $rotatedTokens.refreshToken } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$baseUrl/logout" -ContentType 'application/json' -Body $logoutPayload

[PSCustomObject]@{
    status = 'AUTH_FLOW_OK'
    userId = $registered.userId
    email = $currentUser.email
    role = $currentUser.roles[0]
    accessTokenExpiresInSeconds = $tokens.expiresIn
    refreshTokenRotated = ($tokens.refreshToken -ne $rotatedTokens.refreshToken)
    logoutCompleted = $true
}
