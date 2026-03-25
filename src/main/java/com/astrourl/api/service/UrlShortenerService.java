package com.astrourl.api.service;

import com.astrourl.api.model.ShortUrl;
import com.astrourl.api.model.User;
import com.astrourl.api.repository.BlacklistRepository;
import com.astrourl.api.repository.ShortUrlRepository;
import com.astrourl.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

@Service
public class UrlShortenerService {

    /**
     * Segmentos de ruta reservados para no colisionar con páginas y /api.
     */
    private static final Set<String> RESERVED_SEGMENT = Set.of(
            "api", "login", "register", "dashboard", "css", "js", "images", "webjars",
            "swagger-ui", "swagger-ui.html", "v3", "h2-console", "favicon.ico", "error"
    );
    private static final int MAX_PERSIST_ATTEMPTS = 15;

    private final ShortUrlRepository urlRepository;
    private final BlacklistRepository blacklistRepository;
    private final UserRepository userRepository;
    private final RateLimitService rateLimitService;
    private final ShortCodeGenerator shortCodeGenerator;

    public UrlShortenerService(
            ShortUrlRepository urlRepository,
            BlacklistRepository blacklistRepository,
            UserRepository userRepository,
            RateLimitService rateLimitService,
            ShortCodeGenerator shortCodeGenerator
    ) {
        this.urlRepository = urlRepository;
        this.blacklistRepository = blacklistRepository;
        this.userRepository = userRepository;
        this.rateLimitService = rateLimitService;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    @Transactional
    public String shortenUrl(String longUrl, String clientIp, User user) {
        validateUrl(longUrl);
        if (user == null && rateLimitService.isLimitExceeded(clientIp)) {
            throw new RuntimeException("Límite de invitados excedido.");
        }

        User owner = resolveOwner(user);

        for (int attempt = 0; attempt < MAX_PERSIST_ATTEMPTS; attempt++) {
            String code = shortCodeGenerator.next();
            if (code.length() < ShortCodeGenerator.MIN_LENGTH
                    || RESERVED_SEGMENT.contains(code.toLowerCase())
                    || urlRepository.existsByShortCode(code)) {
                continue;
            }
            ShortUrl urlEntity = new ShortUrl(longUrl, clientIp, owner);
            urlEntity.setShortCode(code);
            ShortUrl saved = urlRepository.save(urlEntity);
            if (saved.getShortCode() == null || saved.getShortCode().length() < ShortCodeGenerator.MIN_LENGTH) {
                throw new IllegalStateException("Código corto inválido tras guardar.");
            }
            return saved.getShortCode();
        }
        throw new RuntimeException("No se pudo generar un código corto único. Inténtalo de nuevo.");
    }

    public String getOriginalUrl(String code) {
        return urlRepository.findByShortCode(code)
                .map(ShortUrl::getLongUrl)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Código no encontrado."));
    }

    private User resolveOwner(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        return userRepository.getReferenceById(user.getId());
    }

    private void validateUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new RuntimeException("Solo se permiten URLs http/https.");
            }
            if (host == null || host.isBlank()) {
                throw new RuntimeException("URL inválida: host no válido.");
            }
            if (host != null && blacklistRepository.existsByDomain(host.toLowerCase())) {
                throw new RuntimeException("Dominio prohibido.");
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("URL inválida.");
        }
    }
}