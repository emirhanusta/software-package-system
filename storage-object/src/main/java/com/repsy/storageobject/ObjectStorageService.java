package com.repsy.storageobject;


import com.repsy.storagecore.StorageService;
import com.repsy.storagecore.exception.FileStorageException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public class ObjectStorageService implements StorageService {

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
        try (InputStream inputStream = file.getInputStream()) {
            String objectName = packageName + "/" + version + "/" + fileName;
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            throw new FileStorageException("File could not be saved to MinIO: " + fileName);
        }
    }

    @Override
    public Resource load(String packageName, String version, String fileName) {
        try {
            String objectName = packageName + "/" + version + "/" + fileName;
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return new InputStreamResource(stream);
        } catch (Exception e) {
            throw new FileStorageException("File could not be loaded from MinIO: " + fileName);
        }
    }
}
