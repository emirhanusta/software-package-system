package com.repsy.repositoryapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repsy.repositoryapi.dto.MetaDto;
import com.repsy.repositoryapi.model.MetaData;
import com.repsy.repositoryapi.repository.PackageDataRepository;
import com.repsy.storagecore.StorageService;
import com.repsy.storagecore.exception.MetaDataMismatchException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PackageService {

    private final PackageDataRepository repository;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    public PackageService(PackageDataRepository repository, @Qualifier("storageService") StorageService storageService,
                          ObjectMapper objectMapper) {
        this.repository = repository;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
    }

    public void upload(String packageName, String version, MultipartFile meta, MultipartFile file) throws Exception {

        MetaDto metaDto = objectMapper.readValue(meta.getBytes(), MetaDto.class);

        if (!metaDto.name().equals(packageName) || !metaDto.version().equals(version)) {
            throw new MetaDataMismatchException("Package name or version in metadata does not match the provided package name or version.");
        }

        storageService.save(packageName, version, "meta.json", meta);

        String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "package.rep";
        storageService.save(packageName, version, originalFileName, file);

        repository.save(MetaData.builder()
                        .name(metaDto.name())
                        .version(metaDto.version())
                        .author(metaDto.author())
                        .dependencies(objectMapper.writeValueAsString(metaDto.dependencies()))
                        .build());
    }


    public Resource download(String packageName, String version, String fileName) {
        return storageService.load(packageName, version, fileName);
    }
}
