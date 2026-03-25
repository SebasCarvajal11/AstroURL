package com.astrourl.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "short_urls")
public class ShortUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String longUrl;
    @Column(unique = true, nullable = false, length = 12)
    private String shortCode;
    private LocalDateTime createdAt;
    private String createdByIp;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public ShortUrl() {}
    public ShortUrl(String longUrl, String createdByIp, User user) {
        this.longUrl = longUrl;
        this.createdByIp = createdByIp;
        this.createdAt = LocalDateTime.now();
        this.user = user;
    }

    public Long getId() { return id; }
    public String getLongUrl() { return longUrl; }
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getUser() { return user; }
}