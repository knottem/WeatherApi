package com.example.weatherapi.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "weather_cache")
public class WeatherCacheEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String cacheKey;

    @Column
    private LocalDateTime timestamp;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "weather_id")
    private WeatherEntity weather;

    public boolean isValid(int hours) {
        return LocalDateTime.now().minusHours(hours).isBefore(timestamp);
    }
}
