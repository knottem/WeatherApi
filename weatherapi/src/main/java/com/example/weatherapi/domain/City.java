package com.example.weatherapi.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
//Simple entity class that represents a city
//Every city has a name, longitude and latitude coordinates
public class City {

    //The id is generated automatically by the database
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private double lon;
    private double lat;

}