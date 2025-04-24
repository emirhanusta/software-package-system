package com.repsy.repositoryapi.repository;

import com.repsy.repositoryapi.model.MetaData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageDataRepository extends JpaRepository<MetaData, Long> {
}
