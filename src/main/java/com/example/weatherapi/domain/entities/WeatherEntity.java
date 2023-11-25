package com.example.weatherapi.domain.entities;

import com.example.weatherapi.domain.weather.Weather;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "weather")
public class WeatherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private LocalDateTime timeStamp;

    @OneToMany(mappedBy = "weatherEntity", cascade = CascadeType.ALL)
    private List<WeatherDataEntity> weatherDataList;


}
