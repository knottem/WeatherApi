package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.ApiStatusDto;
import com.example.weatherapi.repositories.ApiStatusRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/status")
public class StatusController {

    private final ApiStatusRepository apiStatusRepository;

    public StatusController(final ApiStatusRepository apiStatusRepository){
        this.apiStatusRepository = apiStatusRepository;
    }

    @RequestMapping(path = "/api")
    public ResponseEntity<List<ApiStatusDto>> getApiStatus(){
        return ResponseEntity.ok(apiStatusRepository.findAll().stream().map(a ->
                                new ApiStatusDto(a.getApiName(), a.isActive())).toList());
    }
}
