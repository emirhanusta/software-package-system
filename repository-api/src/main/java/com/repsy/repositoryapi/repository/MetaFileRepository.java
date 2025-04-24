package com.repsy.repositoryapi.repository;

import com.repsy.repositoryapi.model.MetaFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaFileRepository extends JpaRepository<MetaFile, Long> {
    boolean existsByNameAndVersion(String name, String version);
}
