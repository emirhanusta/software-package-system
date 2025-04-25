package com.repsy.repositoryapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repsy.repositoryapi.dto.MetaDto;
import com.repsy.repositoryapi.exception.custom.InvalidExtensionException;
import com.repsy.repositoryapi.exception.custom.PackageAlreadyExistsException;
import com.repsy.repositoryapi.model.MetaFile;
import com.repsy.repositoryapi.repository.MetaFileRepository;
import com.repsy.storagecore.StorageService;
import com.repsy.storagecore.exception.MetaDataMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
public class MetaFileService {
    private static final Logger log = LoggerFactory.getLogger(MetaFileService.class);

    private final MetaFileRepository metaFileRepository;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    public MetaFileService(MetaFileRepository repository, @Qualifier("storageService") StorageService storageService,
                           ObjectMapper objectMapper) {
        this.metaFileRepository = repository;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void upload(String packageName, String version, MultipartFile meta, MultipartFile file) throws Exception {
        log.info("Upload request received for package: {}/{}", packageName, version);
        if (meta.isEmpty() || file.isEmpty()) {
            throw new IllegalArgumentException("Meta and package files must not be empty");
        }
        validateFileName(Objects.requireNonNull(meta.getOriginalFilename()));
        validateFileName(Objects.requireNonNull(file.getOriginalFilename()));

        MetaDto metaDto = parseAndValidateMeta(meta, packageName, version);
        checkDuplicate(packageName, version);

        storageService.save(packageName, version, "meta.json", meta);
        storageService.save(packageName, version, "package.rep", file);

        metaFileRepository.save(MetaFile.builder()
                        .name(metaDto.name())
                        .version(metaDto.version())
                        .author(metaDto.author())
                        .dependencies(objectMapper.writeValueAsString(metaDto.dependencies()))
                        .build());
        log.info("Upload completed for: {}/{}", packageName, version);
    }


    public Resource download(String packageName, String version, String fileName) {
        log.info("Download requested: {}/{}/{}", packageName, version, fileName);
        validateFileName(fileName);
        return storageService.load(packageName, version, fileName);
    }

    private void validateFileName(String fileName) {
        if (!fileName.equalsIgnoreCase("package.rep") &&
                !fileName.equalsIgnoreCase("meta.json")) {
            throw new InvalidExtensionException("Invalid file name. Allowed: package.rep, meta.json");
        }
    }

    private void checkDuplicate(String name, String version) {
        if (metaFileRepository.existsByNameAndVersion(name, version)) {
            throw new PackageAlreadyExistsException("Package already exists with the same name and version.");
        }
    }

    private MetaDto parseAndValidateMeta(MultipartFile meta, String expectedName, String expectedVersion) {
        MetaDto dto;
        try {
            dto = objectMapper.readValue(meta.getBytes(), MetaDto.class);
        } catch (Exception e) {
            throw new MetaDataMismatchException("Invalid meta.json format");
        }

        if (dto.name() == null || dto.version() == null || dto.author() == null) {
            throw new MetaDataMismatchException("meta.json must contain name, version, and author");
        }

        if (!dto.name().equals(expectedName) || !dto.version().equals(expectedVersion)) {
            throw new MetaDataMismatchException("Metadata name/version mismatch with path");
        }

        return dto;
    }
}
