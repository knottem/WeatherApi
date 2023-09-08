package com.example.frontend.controllers;

import com.example.frontend.services.impl.WeatherServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@Slf4j
public class WebController {

    WeatherServiceImpl weatherService;

    @Autowired
    public WebController(WeatherServiceImpl weatherService) {
        this.weatherService = weatherService;
    }


    @GetMapping("/{city}")
    public String getWeather(@PathVariable final String city){
        return null;
    }
}
