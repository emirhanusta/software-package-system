package com.repsy.repositoryapi.dto;

import java.util.List;

public record MetaDto(
        String name,
        String version,
        String author,
        List<Dependency> dependencies
) {
    public record Dependency(String packageName, String version) {}
}
