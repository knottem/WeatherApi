package com.example.weatherapi.cache;

import com.example.weatherapi.domain.entities.ApiStatus;
import com.example.weatherapi.repositories.ApiStatusRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class ApiStatusCache {

    private final Logger log;
    private final Cache<String, List<ApiStatus>> apiStatusCache;
    private final ApiStatusRepository apiStatusRepository;

    @Autowired
    public ApiStatusCache(ApiStatusRepository apiStatusRepository) {
        this.apiStatusRepository = apiStatusRepository;
        this.log = LoggerFactory.getLogger(ApiStatusCache.class);
        this.apiStatusCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .build();
    }

    public List<ApiStatus> getAllApiStatuses() {
        return apiStatusCache.get("apiStatus", key -> {
            log.debug("Fetching API status from the database");
            return apiStatusRepository.findAll();
        });
    }

    public Map<String, Boolean> getApiStatus() {
        Map<String, Boolean> apiStatusMap = new HashMap<>();
        List<ApiStatus> apiStatusList = getAllApiStatuses();
        for (ApiStatus apiStatus : apiStatusList) {
            apiStatusMap.put(apiStatus.getApiName(), apiStatus.isActive());
        }
        return apiStatusMap;
    }

    public ApiStatus getApiStatus(String apiName) {
        return getAllApiStatuses().stream()
                .filter(s -> apiName.equalsIgnoreCase(s.getApiName()))
                .findFirst()
                .orElse(null);
    }

    public void invalidateCache() {
        apiStatusCache.invalidateAll();
    }
}
