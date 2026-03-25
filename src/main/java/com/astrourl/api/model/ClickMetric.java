package com.astrourl.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "click_metrics")
public class ClickMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private ShortUrl shortUrl;

    private LocalDateTime clickedAt;
    private String browser;
    private String os;
    private String ipAddress;

    public ClickMetric() {}

    @PrePersist
    protected void onCreate() {
        clickedAt = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public ShortUrl getShortUrl() { return shortUrl; }
    public void setShortUrl(ShortUrl shortUrl) { this.shortUrl = shortUrl; }

    public LocalDateTime getClickedAt() { return clickedAt; }
    public void setClickedAt(LocalDateTime clickedAt) { this.clickedAt = clickedAt; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}