package com.astrourl.api.repository;

import com.astrourl.api.model.ClickMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface ClickMetricRepository extends JpaRepository<ClickMetric, Long> {

    // Contar clicks totales por URL
    long countByShortUrlId(Long urlId);

    // Obtener clicks agrupados por fecha (para el gráfico del dashboard)
    @Query("SELECT CAST(c.clickedAt AS date) as date, COUNT(c) as count " +
            "FROM ClickMetric c WHERE c.shortUrl.id = :urlId " +
            "GROUP BY CAST(c.clickedAt AS date) ORDER BY date ASC")
    List<Map<String, Object>> getClicksByDay(@Param("urlId") Long urlId);

    @Query("SELECT c.shortUrl.id, COUNT(c) FROM ClickMetric c WHERE c.shortUrl.id IN :urlIds GROUP BY c.shortUrl.id")
    List<Object[]> countByShortUrlIds(@Param("urlIds") List<Long> urlIds);

    void deleteByShortUrl_Id(Long urlId);
}