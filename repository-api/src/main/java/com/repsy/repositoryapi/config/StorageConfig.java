package com.repsy.repositoryapi.config;

import com.repsy.storagecore.StorageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {
    @Value("${storage.strategy}")
    private String strategy;

    @Value("${storage.obj.endpoint}") private String endpoint;
    @Value("${storage.obj.access-key}") private String accessKey;
    @Value("${storage.obj.secret-key}") private String secretKey;
    @Value("${storage.obj.bucket}") private String bucket;

    @Value("${storage.fs.base-path:/data/rep-packages}") private String basePath;

    @Bean(name = "fileSystemStorageService")
    public StorageService fileSystemStorageService() {
        return new com.repsy.storagefilesystem.FileSystemStorageService(basePath);
    }

    @Bean(name = "objectStorageService")
    public StorageService objectStorageService() {
        return new com.repsy.storageobject.ObjectStorageService(endpoint, accessKey, secretKey, bucket);
    }

    @Bean
    public StorageService storageService(
            @Qualifier("fileSystemStorageService") StorageService fileSystem,
            @Qualifier("objectStorageService") StorageService objectStorage
    ) {
        return switch (strategy) {
            case "file-system" -> fileSystem;
            case "object-storage" -> objectStorage;
            default -> throw new IllegalArgumentException("Invalid storage strategy: " + strategy);
        };
    }
}
