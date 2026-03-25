package com.astrourl.api.controller;

import com.astrourl.api.dto.UrlResponseDTO;
import com.astrourl.api.model.User;
import com.astrourl.api.repository.ClickMetricRepository;
import com.astrourl.api.repository.ShortUrlRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final ShortUrlRepository urlRepository;
    private final ClickMetricRepository metricRepository;

    public DashboardController(ShortUrlRepository urlRepository, ClickMetricRepository metricRepository) {
        this.urlRepository = urlRepository;
        this.metricRepository = metricRepository;
    }

    @GetMapping("/urls")
    public ResponseEntity<List<UrlResponseDTO>> getUserUrls(@AuthenticationPrincipal User user) {
        var urls = urlRepository.findByUser_Id(user.getId());
        List<Long> urlIds = urls.stream().map(url -> url.getId()).toList();
        Map<Long, Long> clickMap = urlIds.isEmpty()
                ? Map.of()
                : metricRepository.countByShortUrlIds(urlIds).stream()
                        .collect(Collectors.toMap(
                                row -> ((Number) row[0]).longValue(),
                                row -> ((Number) row[1]).longValue()
                        ));

        List<UrlResponseDTO> response = urls.stream()
                .map(url -> new UrlResponseDTO(
                        url.getId(),
                        url.getShortCode(),
                        url.getLongUrl(),
                        url.getCreatedAt(),
                        clickMap.getOrDefault(url.getId(), 0L)
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/urls/{id}")
    public ResponseEntity<?> deleteUrl(@PathVariable Long id, @AuthenticationPrincipal User user) {
        var optionalUrl = urlRepository.findById(id);
        if (optionalUrl.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "URL no encontrada."));
        }
        var url = optionalUrl.get();

        if (url.getUser() == null || !url.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "No autorizado para eliminar esta URL."));
        }

        metricRepository.deleteByShortUrl_Id(id);
        urlRepository.delete(url);
        return ResponseEntity.ok(Map.of("message", "Eliminado"));
    }
}