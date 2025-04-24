package com.repsy.storagecore;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void save(String packageName, String version, String fileName, MultipartFile file);
    Resource load(String packageName, String version, String fileName);
}
