package com.predix.compliance.repository;

import com.predix.compliance.domain.CountryProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryProfileRepository extends JpaRepository<CountryProfileEntity, String> {
}
