package com.greenocean.backend.post.dto;
import java.util.List;
public record SocialFeedResponse(List<PostResponse> items,boolean hasNext,String nextCursor) {}
