package com.repsy.storagefilesystem;

import com.repsy.storagecore.StorageService;
import com.repsy.storagecore.exception.DirectoryCreationException;
import com.repsy.storagecore.exception.ResourceNotFoundException;
import com.repsy.storagecore.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public class FileSystemStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(FileSystemStorageService.class);
    private final String basePath;

    public FileSystemStorageService(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public void save(String packageName, String version, String fileName, MultipartFile file) {
        try {
            File directory = new File(basePath + "/" + packageName + "/" + version).getAbsoluteFile();
            if (!directory.exists()) {
                log.debug("Creating directory: {}", directory.getAbsolutePath());
                boolean created = directory.mkdirs();
                if (!created) {
                    log.error("Failed to create directory: {}", directory.getAbsolutePath());
                    throw new DirectoryCreationException("Directory could not be created: " + directory.getAbsolutePath());
                }
            }

            File destination = new File(directory, fileName);
            log.info("Saving file to: {}", destination.getAbsolutePath());
            file.transferTo(destination);

            log.info("File saved successfully: {}", fileName);

        } catch (IOException e) {
            log.error("Error saving file: {}", fileName, e);
            throw new FileStorageException("Failed to save file: " + fileName);
        }
    }

    @Override
    public Resource load(String packageName, String version, String fileName) {
        File file = resolvePath(packageName, version, fileName);
        if (!file.exists()) {
            log.warn("Requested file not found: {}", file.getAbsolutePath());
            throw new ResourceNotFoundException("File not found with name: " + fileName);
        }
        log.info("File loaded: {}", file.getAbsolutePath());
        return new FileSystemResource(file);
    }

    private File resolvePath(String packageName, String version, String fileName) {
        return new File(basePath + "/" + packageName + "/" + version + "/" + fileName).getAbsoluteFile();
    }

}
