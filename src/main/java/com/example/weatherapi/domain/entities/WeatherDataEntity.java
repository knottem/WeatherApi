package com.example.weatherapi.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "weather_data")
public class WeatherDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @Column
    private ZonedDateTime validTime;

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
