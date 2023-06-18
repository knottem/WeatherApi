package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.exceptions.CityNotFoundException;
import com.example.weatherapi.services.CityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CityController.class)
public class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CityService cityService;

    @BeforeEach
    public void setUp() {
        when(cityService.getCityByName("Stockholm")).thenReturn(new City(1L, "Stockholm", 59.3293, 18.0686));
        when(cityService.getCityByName("Uppsala")).thenReturn(new City(2L, "Uppsala", 59.8586, 17.6389));
        when(cityService.getCityByName("Karlstad")).thenReturn(new City(3L, "Karlstad", 59.3793, 13.5036));
        when(cityService.getCityByName("cityNotFound")).thenThrow(new CityNotFoundException("City not found: cityNotFound"));
    }

    @AfterEach
    public void tearDown() {
        cityService = null;
    }

    @Test
    public void retrieveCityTestValid() throws Exception {
        mockMvc.perform(get("/city/Stockholm")
                        .contentType("application/json")
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Stockholm"))
                .andExpect(jsonPath("$.lon").value(59.3293))
                .andExpect(jsonPath("$.lat").value(18.0686));
    }

    @Test
    public void retrieveCityTestFaulty() throws Exception {
        mockMvc.perform(get("/city/cityNotFound")
                        .contentType("application/json")
                        .with(httpBasic("user", "password")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("City not found: cityNotFound"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }
}
