package com.astrourl.api.service;

import com.astrourl.api.model.ClickMetric;
import com.astrourl.api.model.ShortUrl;
import com.astrourl.api.repository.ClickMetricRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AnalyticsService {

    private final ClickMetricRepository metricRepository;

    // Constructor manual
    public AnalyticsService(ClickMetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    @Async
    public void recordClick(ShortUrl url, String userAgent, String ip) {
        String browser = "Unknown";
        String os = "Unknown";

        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            // Chromium Edge incluye "chrome" en el UA; Opera incluye "opr/".
            if (ua.contains("edg")) browser = "Edge";
            else if (ua.contains("opr") || ua.contains("opera")) browser = "Opera";
            else if (ua.contains("chrome")) browser = "Chrome";
            else if (ua.contains("firefox")) browser = "Firefox";
            else if (ua.contains("safari")) browser = "Safari";

            if (ua.contains("windows")) os = "Windows";
            else if (ua.contains("android")) os = "Android";
            else if (ua.contains("iphone")) os = "iOS";
            else if (ua.contains("linux")) os = "Linux";
        }

        ClickMetric metric = new ClickMetric();
        metric.setShortUrl(url);
        metric.setBrowser(browser);
        metric.setOs(os);
        metric.setIpAddress(ip);
        metric.setClickedAt(LocalDateTime.now());

        metricRepository.save(metric);
    }
}