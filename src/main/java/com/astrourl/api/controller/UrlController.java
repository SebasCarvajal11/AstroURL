package com.astrourl.api.controller;

import com.astrourl.api.dto.ShortenUrlRequestDTO;
import com.astrourl.api.model.ShortUrl;
import com.astrourl.api.model.User;
import com.astrourl.api.repository.ShortUrlRepository;
import com.astrourl.api.service.AnalyticsService;
import com.astrourl.api.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping
public class UrlController {

    private final UrlShortenerService urlService;
    private final ShortUrlRepository urlRepository;
    private final AnalyticsService analyticsService;

    // Constructor manual
    public UrlController(UrlShortenerService urlService, ShortUrlRepository urlRepository, AnalyticsService analyticsService) {
        this.urlService = urlService;
        this.urlRepository = urlRepository;
        this.analyticsService = analyticsService;
    }

    @PostMapping("/api/v1/shorten")
    public ResponseEntity<?> shorten(@Valid @RequestBody ShortenUrlRequestDTO request, HttpServletRequest servletRequest) {
        String longUrl = request.url();
        String ip = servletRequest.getRemoteAddr();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;

        if (auth != null && auth.getPrincipal() instanceof User) {
            currentUser = (User) auth.getPrincipal();
        }

        String code = urlService.shortenUrl(longUrl, ip, currentUser);
        String shortUrl = Objects.requireNonNull(
                ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/{code}")
                        .buildAndExpand(code)
                        .toUriString()
        );

        return ResponseEntity.ok(Map.of(
                "shortUrl", shortUrl,
                "code", code,
                "isRegistered", currentUser != null
        ));
    }

    /**
     * Solo códigos alfanuméricos (longitud acorde al generador y a la columna).
     * Evita interceptar /favicon.ico, /robots.txt, u otros segmentos con puntos o rutas cortas tipo /js.
     */
    @GetMapping("/{code:[A-Za-z0-9]{4,12}}")
    public RedirectView redirect(@PathVariable String code, HttpServletRequest request) {
        ShortUrl url = urlRepository.findByShortCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coordenada no encontrada"));

        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();
        analyticsService.recordClick(url, userAgent, ip);

        return new RedirectView(url.getLongUrl());
    }
}