package com.example.weatherapi.domain.entities;

import jakarta.persistence.*;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "city")
//Simple entity class that represents a city
//Every city has a name, longitude and latitude coordinates
public class CityEntity {

    //The id is generated automatically by the database
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double lon;
    private Double lat;

}