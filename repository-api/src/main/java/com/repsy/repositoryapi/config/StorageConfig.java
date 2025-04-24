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

    @Bean(name = "fileSystemStorageService")
    public StorageService fileSystemStorageService(
            @Value("${storage.fs.base-path:/data/rep-packages}") String basePath
    ) {
        return new com.repsy.storagefilesystem.FileSystemStorageService(basePath);
    }

    @Bean(name = "objectStorageService")
    public StorageService objectStorageService(
            @Value("${storage.obj.endpoint}") String endpoint,
            @Value("${storage.obj.access-key}") String accessKey,
            @Value("${storage.obj.secret-key}") String secretKey,
            @Value("${storage.obj.bucket}") String bucket
    ) {
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
