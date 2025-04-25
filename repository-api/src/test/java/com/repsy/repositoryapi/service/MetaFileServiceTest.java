package com.repsy.repositoryapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repsy.repositoryapi.exception.custom.InvalidExtensionException;
import com.repsy.repositoryapi.exception.custom.PackageAlreadyExistsException;
import com.repsy.repositoryapi.model.MetaFile;
import com.repsy.repositoryapi.repository.MetaFileRepository;
import com.repsy.storagecore.StorageService;
import com.repsy.storagecore.exception.MetaDataMismatchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetaFileServiceTest {

    @Mock
    private MetaFileRepository metaFileRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private MetaFileService metaFileService;

    @Captor
    private ArgumentCaptor<MetaFile> metaFileCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        metaFileService = new MetaFileService(metaFileRepository, storageService, objectMapper);
    }

    @Test
    void shouldThrowWhenMetaOrFileEmpty() {
        MultipartFile empty = new MockMultipartFile("file", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> metaFileService.upload("pkg", "1.0.0", empty, empty));
    }

    @Test
    void shouldThrowWhenInvalidFileName() {
        MultipartFile meta = new MockMultipartFile("meta.json", "meta.json", "application/json", validMetaJson().getBytes());
        MultipartFile invalid = new MockMultipartFile("something.txt", "something.txt", "text/plain", "data".getBytes());

        assertThrows(InvalidExtensionException.class,
                () -> metaFileService.upload("pkg", "1.0.0", meta, invalid));
    }

    @Test
    void shouldThrowWhenMetaParseFails() {
        MultipartFile brokenMeta = new MockMultipartFile("meta.json", "meta.json", "application/json", "INVALID_JSON".getBytes());
        MultipartFile file = new MockMultipartFile("package.rep", "package.rep", "application/octet-stream", "test".getBytes());

        assertThrows(MetaDataMismatchException.class,
                () -> metaFileService.upload("pkg", "1.0.0", brokenMeta, file));
    }

    @Test
    void shouldThrowWhenMetaFieldMissing() {
        String json = """
                {
                    "name": "pkg",
                    "version": "1.0.0"
                }
                """;
        MultipartFile meta = new MockMultipartFile("meta.json", "meta.json", "application/json", json.getBytes());
        MultipartFile file = new MockMultipartFile("package.rep", "package.rep", "application/octet-stream", "test".getBytes());

        assertThrows(MetaDataMismatchException.class,
                () -> metaFileService.upload("pkg", "1.0.0", meta, file));
    }

    @Test
    void shouldThrowWhenNameVersionMismatch() {
        String json = """
                {
                    "name": "wrong",
                    "version": "1.0.0",
                    "author": "Emirhan"
                }
                """;
        MultipartFile meta = new MockMultipartFile("meta.json", "meta.json", "application/json", json.getBytes());
        MultipartFile file = new MockMultipartFile("package.rep", "package.rep", "application/octet-stream", "test".getBytes());

        assertThrows(MetaDataMismatchException.class,
                () -> metaFileService.upload("pkg", "1.0.0", meta, file));
    }

    @Test
    void shouldThrowWhenPackageAlreadyExists() {
        when(metaFileRepository.existsByNameAndVersion("pkg", "1.0.0")).thenReturn(true);

        MultipartFile meta = new MockMultipartFile("meta.json", "meta.json", "application/json", validMetaJson().getBytes());
        MultipartFile file = new MockMultipartFile("package.rep", "package.rep", "application/octet-stream", "test".getBytes());

        assertThrows(PackageAlreadyExistsException.class,
                () -> metaFileService.upload("pkg", "1.0.0", meta, file));
    }

    @Test
    void shouldUploadSuccessfully() throws Exception {
        when(metaFileRepository.existsByNameAndVersion("pkg", "1.0.0")).thenReturn(false);

        MultipartFile meta = new MockMultipartFile("meta.json", "meta.json", "application/json", validMetaJson().getBytes());
        MultipartFile file = new MockMultipartFile("package.rep", "package.rep", "application/octet-stream", "test".getBytes());

        metaFileService.upload("pkg", "1.0.0", meta, file);

        verify(storageService).save("pkg", "1.0.0", "meta.json", meta);
        verify(storageService).save("pkg", "1.0.0", "package.rep", file);
        verify(metaFileRepository).save(metaFileCaptor.capture());

        MetaFile saved = metaFileCaptor.getValue();
        assertEquals("pkg", saved.getName());
        assertEquals("1.0.0", saved.getVersion());
    }

    @Test
    void shouldThrowOnDownloadInvalidFile() {
        assertThrows(InvalidExtensionException.class,
                () -> metaFileService.download("pkg", "1.0.0", "evil.exe"));
    }

    private String validMetaJson() {
        return """
                {
                    "name": "pkg",
                    "version": "1.0.0",
                    "author": "Emirhan",
                    "dependencies": []
                }
                """;
    }
}
