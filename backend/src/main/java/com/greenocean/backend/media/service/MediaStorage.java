package com.greenocean.backend.media.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MediaStorage {
    void store(String key, MultipartFile file) throws IOException;
    Resource load(String key);
    void delete(String key) throws IOException;
}
