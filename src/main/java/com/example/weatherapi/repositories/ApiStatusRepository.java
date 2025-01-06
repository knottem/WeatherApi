package com.example.weatherapi.repositories;

import com.example.weatherapi.domain.entities.ApiStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApiStatusRepository extends JpaRepository<ApiStatus, UUID> {

    ApiStatus findByApiName(String apiName);

}
