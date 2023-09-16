package com.example.weatherapi.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "city")
//Simple entity class that represents a city
//Every city has a name, longitude and latitude coordinates
public class CityEntity {

    //The id is generated automatically by the database
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double lon;
    private Double lat;

}