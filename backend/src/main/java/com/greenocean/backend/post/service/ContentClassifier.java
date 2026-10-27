package com.greenocean.backend.post.service;
/** Implementations must be local unless a separately approved processing contract permits a provider. */
public interface ContentClassifier {
    ContentSafety.Assessment assess(String body);
}
