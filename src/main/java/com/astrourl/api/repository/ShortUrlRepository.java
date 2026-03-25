package com.astrourl.api.repository;

import com.astrourl.api.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCode(String shortCode);
    List<ShortUrl> findByUser_Id(Long userId);
    boolean existsByShortCode(String shortCode);
}