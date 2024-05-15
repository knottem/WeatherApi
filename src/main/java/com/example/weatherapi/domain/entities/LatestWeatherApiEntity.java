package com.example.weatherapi.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "latest_weather_api")
public class LatestWeatherApiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "latest_weather_id", referencedColumnName = "id")
    private WeatherEntity latestWeather;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity city;

    @Column(nullable = false)
    private boolean smhi;

    @Column(nullable = false)
    private boolean yr;

    @Column(nullable = false)
    private boolean fmi;

}
