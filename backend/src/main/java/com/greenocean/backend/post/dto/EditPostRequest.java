package com.greenocean.backend.post.dto;
import jakarta.validation.constraints.*;
public record EditPostRequest(@NotBlank @Size(max=10000) String body, @Min(1) long revision) {}
