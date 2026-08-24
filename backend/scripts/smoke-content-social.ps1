$ErrorActionPreference = 'Stop'

function Assert-True {
    param(
        [bool]$Condition,
        [string]$Message
    )

    if (-not $Condition) {
        throw "Smoke assertion failed: $Message"
    }
}

function New-SmokeUser {
    param(
        [string]$Prefix,
        [string]$RunId,
        [string]$ApiUrl
    )

    $email = "$Prefix-$RunId@example.test"
    $username = "$Prefix$RunId"
    $password = 'StrongPassword_2026'
    $registerPayload = @{
        email = $email
        password = $password
        username = $username
        displayName = "$Prefix smoke user"
        birthYear = 2000
        countryCode = 'IR'
        city = 'Tehran'
    } | ConvertTo-Json

    $registered = Invoke-RestMethod -Method Post -Uri "$ApiUrl/auth/register" -ContentType 'application/json' -Body $registerPayload
    $loginPayload = @{
        email = $email
        password = $password
        deviceName = 'Content and social smoke test'
    } | ConvertTo-Json
    $tokens = Invoke-RestMethod -Method Post -Uri "$ApiUrl/auth/login" -ContentType 'application/json' -Body $loginPayload

    return [PSCustomObject]@{
        id = $registered.userId
        username = $username
        headers = @{ Authorization = "Bearer $($tokens.accessToken)" }
    }
}

$runId = Get-Date -Format 'yyyyMMddHHmmssfff'
$apiUrl = 'http:' + '//' + 'localhost:8080' + '/api/v1'
$author = New-SmokeUser -Prefix 'author' -RunId $runId -ApiUrl $apiUrl
$reader = New-SmokeUser -Prefix 'reader' -RunId $runId -ApiUrl $apiUrl

$publicPostPayload = @{
    body = "GreenOcean support story $runId"
    anonymous = $false
    visibility = 'PUBLIC'
} | ConvertTo-Json
$publicPost = Invoke-RestMethod -Method Post -Uri "$apiUrl/posts" -Headers $author.headers -ContentType 'application/json' -Body $publicPostPayload

$followersPostPayload = @{
    body = "Followers support story $runId"
    anonymous = $false
    visibility = 'FOLLOWERS'
} | ConvertTo-Json
$followersPost = Invoke-RestMethod -Method Post -Uri "$apiUrl/posts" -Headers $author.headers -ContentType 'application/json' -Body $followersPostPayload

$feedBeforeFollow = Invoke-RestMethod -Method Get -Uri "$apiUrl/posts/feed" -Headers $reader.headers
$feedBeforeIds = @($feedBeforeFollow.items | ForEach-Object { $_.id })
Assert-True ($feedBeforeIds -contains $publicPost.id) 'public post must be visible before following'
Assert-True (-not ($feedBeforeIds -contains $followersPost.id)) 'followers-only post must be hidden before following'

Invoke-RestMethod -Method Put -Uri "$apiUrl/social/follows/$($author.id)" -Headers $reader.headers | Out-Null
$feedAfterFollow = Invoke-RestMethod -Method Get -Uri "$apiUrl/posts/feed" -Headers $reader.headers
$feedAfterIds = @($feedAfterFollow.items | ForEach-Object { $_.id })
Assert-True ($feedAfterIds -contains $publicPost.id) 'public post must remain visible after following'
Assert-True ($feedAfterIds -contains $followersPost.id) 'followers-only post must become visible after following'

Invoke-RestMethod -Method Put -Uri "$apiUrl/posts/$($publicPost.id)/like" -Headers $reader.headers | Out-Null
Invoke-RestMethod -Method Put -Uri "$apiUrl/posts/$($publicPost.id)/bookmark" -Headers $reader.headers | Out-Null

$commentPayload = @{
    body = 'You are not alone.'
    anonymous = $false
} | ConvertTo-Json
$comment = Invoke-RestMethod -Method Post -Uri "$apiUrl/posts/$($publicPost.id)/comments" -Headers $reader.headers -ContentType 'application/json' -Body $commentPayload

$replyPayload = @{
    body = 'Thank you for the support.'
    parentCommentId = $comment.id
    anonymous = $false
} | ConvertTo-Json
$reply = Invoke-RestMethod -Method Post -Uri "$apiUrl/posts/$($publicPost.id)/comments" -Headers $author.headers -ContentType 'application/json' -Body $replyPayload
Invoke-RestMethod -Method Put -Uri "$apiUrl/comments/$($comment.id)/like" -Headers $author.headers | Out-Null

$postDetail = Invoke-RestMethod -Method Get -Uri "$apiUrl/posts/$($publicPost.id)" -Headers $reader.headers
Assert-True $postDetail.liked 'post must be liked by the reader'
Assert-True $postDetail.bookmarked 'post must be bookmarked by the reader'
Assert-True ($postDetail.likeCount -eq 1) 'post like count must be one'
Assert-True ($postDetail.commentCount -eq 2) 'post comment count must include comment and reply'

$comments = Invoke-RestMethod -Method Get -Uri "$apiUrl/posts/$($publicPost.id)/comments" -Headers $reader.headers
$commentIds = @($comments.items | ForEach-Object { $_.id })
Assert-True ($commentIds -contains $comment.id) 'comment must be returned by the post comments endpoint'
Assert-True ($commentIds -contains $reply.id) 'reply must be returned by the post comments endpoint'

$search = Invoke-RestMethod -Method Get -Uri "$apiUrl/search/posts?q=$runId" -Headers $reader.headers
$searchIds = @($search.items | ForEach-Object { $_.id })
Assert-True ($searchIds -contains $publicPost.id) 'post search must return the matching public post'

$communityPayload = @{
    name = "Smoke Support $runId"
    slug = "smoke-support-$runId"
    description = 'A safe smoke-test support circle.'
    privateCommunity = $false
} | ConvertTo-Json
$community = Invoke-RestMethod -Method Post -Uri "$apiUrl/communities" -Headers $author.headers -ContentType 'application/json' -Body $communityPayload
$joinedCommunity = Invoke-RestMethod -Method Put -Uri "$apiUrl/communities/$($community.id)/membership" -Headers $reader.headers
Assert-True $joinedCommunity.member 'reader must become a community member'
Assert-True ($joinedCommunity.memberCount -eq 2) 'community must contain owner and reader'

$communityPostPayload = @{
    body = "Community support story $runId"
    anonymous = $false
    visibility = 'COMMUNITY'
    communityId = $community.id
} | ConvertTo-Json
$communityPost = Invoke-RestMethod -Method Post -Uri "$apiUrl/posts" -Headers $author.headers -ContentType 'application/json' -Body $communityPostPayload
$communityFeed = Invoke-RestMethod -Method Get -Uri "$apiUrl/communities/$($community.id)/posts" -Headers $reader.headers
$communityPostIds = @($communityFeed.items | ForEach-Object { $_.id })
Assert-True ($communityPostIds -contains $communityPost.id) 'community feed must contain the community post'

$notifications = Invoke-RestMethod -Method Get -Uri "$apiUrl/notifications" -Headers $author.headers
$notificationTypes = @($notifications.items | ForEach-Object { $_.type })
Assert-True ($notificationTypes -contains 'FOLLOW') 'follow must create a notification'
Assert-True ($notificationTypes -contains 'LIKE') 'post like must create a notification'
Assert-True ($notificationTypes -contains 'COMMENT') 'post comment must create a notification'
$unread = Invoke-RestMethod -Method Get -Uri "$apiUrl/notifications/unread-count" -Headers $author.headers
Assert-True ($unread.count -ge 3) 'author must have at least three unread notifications'
Invoke-RestMethod -Method Put -Uri "$apiUrl/notifications/read-all" -Headers $author.headers | Out-Null
$unreadAfterMarkAll = Invoke-RestMethod -Method Get -Uri "$apiUrl/notifications/unread-count" -Headers $author.headers
Assert-True ($unreadAfterMarkAll.count -eq 0) 'mark-all-read must clear the unread count'

Invoke-RestMethod -Method Put -Uri "$apiUrl/social/blocks/$($author.id)" -Headers $reader.headers | Out-Null
$feedAfterBlock = Invoke-RestMethod -Method Get -Uri "$apiUrl/posts/feed" -Headers $reader.headers
$blockedFeedIds = @($feedAfterBlock.items | ForEach-Object { $_.id })
Assert-True (-not ($blockedFeedIds -contains $publicPost.id)) 'blocked author posts must disappear from the feed'
Assert-True (-not ($blockedFeedIds -contains $followersPost.id)) 'blocked author followers posts must disappear from the feed'

$userSearch = Invoke-RestMethod -Method Get -Uri "$apiUrl/search/users?q=$($author.username)" -Headers $reader.headers
Assert-True (@($userSearch.items).Count -eq 0) 'blocked account must disappear from user search'

[PSCustomObject]@{
    status = 'CONTENT_SOCIAL_FLOW_OK'
    authorId = $author.id
    readerId = $reader.id
    publicPostId = $publicPost.id
    followersPostId = $followersPost.id
    commentId = $comment.id
    replyId = $reply.id
    feedVisibilityEnforced = $true
    interactionsVerified = $true
    searchVerified = $true
    communityVerified = $true
    notificationsVerified = $true
    blockPrivacyVerified = $true
}
