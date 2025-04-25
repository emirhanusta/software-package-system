package com.repsy.repositoryapi.controller;

import com.repsy.repositoryapi.service.MetaFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MetaFileController.class)
public class MetaFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetaFileService metaFileService;

    private MockMultipartFile metaFile;
    private MockMultipartFile packageFile;

    @BeforeEach
    public void setUp() {
        metaFile = new MockMultipartFile(
                "meta", "meta.json", "application/json", "{\"name\":\"test\"}".getBytes());

        packageFile = new MockMultipartFile(
                "package", "package.tar.gz", "application/gzip", "dummy content".getBytes());
    }

    @Test
    public void testUploadPackage() throws Exception {
        mockMvc.perform(multipart("/api/test-package/1.0.0")
                        .file(metaFile)
                        .file(packageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Package uploaded successfully"));

        Mockito.verify(metaFileService)
                .upload(eq("test-package"), eq("1.0.0"), eq(metaFile), eq(packageFile));
    }

    @Test
    public void testDownloadMetaFile() throws Exception {
        String fileName = "meta.json";
        byte[] fileContent = "{\"name\":\"test\"}".getBytes();
        Resource resource = new ByteArrayResource(fileContent);

        Mockito.when(metaFileService.download("test-package", "1.0.0", fileName))
                .thenReturn(resource);

        mockMvc.perform(get("/api/test-package/1.0.0/meta.json"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"meta.json\""))
                .andExpect(content().bytes(fileContent));
    }

    @Test
    public void testDownloadBinaryPackage() throws Exception {
        String fileName = "package.tar.gz";
        byte[] fileContent = "binary data".getBytes();
        Resource resource = new ByteArrayResource(fileContent);

        Mockito.when(metaFileService.download("test-package", "1.0.0", fileName))
                .thenReturn(resource);

        mockMvc.perform(get("/api/test-package/1.0.0/package.tar.gz"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"package.tar.gz\""))
                .andExpect(content().bytes(fileContent));
    }
}
