package com.repsy.storagefilesystem;

import com.repsy.storagecore.StorageService;
import com.repsy.storagecore.exception.DirectoryCreationException;
import com.repsy.storagecore.exception.ResourceNotFoundException;
import com.repsy.storagecore.exception.FileStorageException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public class FileSystemStorageService implements StorageService {

    private final String basePath;

    public FileSystemStorageService(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public void save(String packageName, String version, String fileName, MultipartFile file) {
        try {
            File directory = new File(basePath + "/" + packageName + "/" + version).getAbsoluteFile();
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new DirectoryCreationException("Directory could not be created: " + directory.getAbsolutePath());
                }
            }
            File destination = new File(directory, fileName);
            file.transferTo(destination);
        } catch (IOException e) {
            throw new FileStorageException("Failed to save file: " + fileName);
        }
    }

    @Override
    public Resource load(String packageName, String version, String fileName) {
        File file = new File(basePath + "/" + packageName + "/" + version + "/" + fileName);
        if (!file.exists()) {
            throw new ResourceNotFoundException("File not found with name: " + fileName);
        }
        return new FileSystemResource(file);
    }
}
