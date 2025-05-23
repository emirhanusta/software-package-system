package com.repsy.repositoryapi.controller;

import com.repsy.repositoryapi.service.MetaFileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class MetaFileController {

    private final MetaFileService metaFileService;

    public MetaFileController(MetaFileService metaFileService) {
        this.metaFileService = metaFileService;
    }

    @PostMapping("/{packageName}/{version}")
    public ResponseEntity<String> upload(
            @PathVariable String packageName,
            @PathVariable String version,
            @RequestPart("meta") MultipartFile metaFile,
            @RequestPart("package") MultipartFile packageFile
    ) throws Exception {
        metaFileService.upload(packageName, version, metaFile, packageFile);
        return ResponseEntity.ok("Package uploaded successfully");
    }

    @GetMapping("/{packageName}/{version}/{fileName}")
    public ResponseEntity<Resource> download(
            @PathVariable String packageName,
            @PathVariable String version,
            @PathVariable String fileName) {

        Resource file = metaFileService.download(packageName, version, fileName);

        String contentType = fileName.endsWith(".json")
                ? "application/json"
                : "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(file);
    }

}
