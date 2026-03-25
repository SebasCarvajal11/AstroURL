package com.astrourl.api.repository;

import com.astrourl.api.model.BlacklistedDomain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistRepository extends JpaRepository<BlacklistedDomain, Long> {
    boolean existsByDomain(String domain);
}