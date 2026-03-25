package com.astrourl.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "blacklist")
public class BlacklistedDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String domain;

    public BlacklistedDomain() {}

    public Long getId() { return id; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
}