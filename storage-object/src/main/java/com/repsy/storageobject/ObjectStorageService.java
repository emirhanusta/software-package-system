package com.repsy.storageobject;


import com.repsy.storagecore.StorageService;
import com.repsy.storagecore.exception.FileStorageException;
import com.repsy.storagecore.exception.ResourceNotFoundException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public class ObjectStorageService implements StorageService {
    private final Logger log = LoggerFactory.getLogger(ObjectStorageService.class);

    private final MinioClient minioClient;
    private final String bucketName;

    public ObjectStorageService(String endpoint, String accessKey, String secretKey, String bucketName) {
        this.bucketName = bucketName;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Override
    public void save(String packageName, String version, String fileName, MultipartFile file) {
        String objectName = packageName + "/" + version + "/" + fileName;
        log.info("Saving object to MinIO: bucket={}, object={}", bucketName, objectName);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("Successfully saved object: {}", objectName);
        } catch (Exception e) {
            log.error("Error while saving object to MinIO: {}", objectName, e);
            throw new FileStorageException("Failed to save object: " + objectName);
        }
    }


    @Override
    public Resource load(String packageName, String version, String fileName) {
        String objectName = packageName + "/" + version + "/" + fileName;
        log.info("Loading object from MinIO: bucket={}, object={}", bucketName, objectName);

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("Successfully loaded object: {}", objectName);
            return new InputStreamResource(stream);
        } catch (Exception e) {
            log.warn("Object not found in MinIO: {}", objectName);
            throw new ResourceNotFoundException("Object not found in MinIO: " + objectName);
        }
    }

}
