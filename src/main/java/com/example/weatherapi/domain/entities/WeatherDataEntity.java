package com.example.weatherapi.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "weather_data")
public class WeatherDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDateTime validTime;

    @Column
    private float temperature;

    @Column
    private int weatherCode;

    @Column
    private float windSpeed;

    @Column
    private float windDirection;

    @Column
    private float precipitation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weather_id")
    private WeatherEntity weatherEntity;

}
