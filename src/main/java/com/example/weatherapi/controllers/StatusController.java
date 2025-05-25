package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.dto.ApiStatusDto;
import com.example.weatherapi.repositories.ApiStatusRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/status")
@Tag(name = "Status", description = "Status endpoint")
public class StatusController {

    private final ApiStatusRepository apiStatusRepository;

    public StatusController(final ApiStatusRepository apiStatusRepository){
        this.apiStatusRepository = apiStatusRepository;
    }

    @Operation(
            summary = "Get Status of all APis",
            description = "Returns the status of all the apis that are used for this api"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved status data"),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(path = "/api")
    public ResponseEntity<List<ApiStatusDto>> getApiStatus(){
        return ResponseEntity.ok(apiStatusRepository.findAll().stream().map(a -> new ApiStatusDto(a.getApiName(), a.isActive())).toList());
    }
}
