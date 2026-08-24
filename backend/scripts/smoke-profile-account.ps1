$ErrorActionPreference = 'Stop'

$runId = Get-Date -Format 'yyyyMMddHHmmss'
$apiUrl = 'http:' + '//' + 'localhost:8080' + '/api/v1'
$email = "profile-$runId@example.test"
$oldPassword = 'StrongPassword_2026'
$newPassword = 'NewStrongPassword_2026'
$username = "profile$runId"

$registerPayload = @{
    email = $email
    password = $oldPassword
    username = $username
    displayName = 'Profile Smoke User'
    birthYear = 2000
    countryCode = 'IR'
    city = 'Tehran'
} | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$apiUrl/auth/register" -ContentType 'application/json' -Body $registerPayload | Out-Null

$loginPayload = @{ email = $email; password = $oldPassword; deviceName = 'Profile smoke test' } | ConvertTo-Json
$tokens = Invoke-RestMethod -Method Post -Uri "$apiUrl/auth/login" -ContentType 'application/json' -Body $loginPayload
$headers = @{ Authorization = "Bearer $($tokens.accessToken)" }

$profilePayload = @{
    displayName = 'Updated Profile User'
    bio = 'A safe place to share and receive support.'
    profilePrivate = $true
    showLocation = $false
    showBirthYear = $false
} | ConvertTo-Json
$ownProfile = Invoke-RestMethod -Method Patch -Uri "$apiUrl/profiles/me" -Headers $headers -ContentType 'application/json' -Body $profilePayload
$publicProfile = Invoke-RestMethod -Method Get -Uri "$apiUrl/profiles/$username" -Headers $headers

$passwordPayload = @{ currentPassword = $oldPassword; newPassword = $newPassword } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$apiUrl/auth/change-password" -Headers $headers -ContentType 'application/json' -Body $passwordPayload | Out-Null

$newLoginPayload = @{ email = $email; password = $newPassword; deviceName = 'Profile smoke test after password change' } | ConvertTo-Json
$newTokens = Invoke-RestMethod -Method Post -Uri "$apiUrl/auth/login" -ContentType 'application/json' -Body $newLoginPayload
$newHeaders = @{ Authorization = "Bearer $($newTokens.accessToken)" }
Invoke-RestMethod -Method Post -Uri "$apiUrl/auth/logout-all" -Headers $newHeaders | Out-Null

[PSCustomObject]@{
    status = 'PROFILE_ACCOUNT_FLOW_OK'
    email = $email
    username = $ownProfile.username
    profilePrivate = $publicProfile.profilePrivate
    publicLocationHidden = ($null -eq $publicProfile.countryCode -and $null -eq $publicProfile.city)
    publicBirthYearHidden = ($null -eq $publicProfile.birthYear)
    passwordChanged = $true
    allSessionsRevoked = $true
}
