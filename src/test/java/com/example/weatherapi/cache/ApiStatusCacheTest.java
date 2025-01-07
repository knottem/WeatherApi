package com.example.weatherapi.cache;

import com.example.weatherapi.domain.entities.ApiStatus;
import com.example.weatherapi.repositories.ApiStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiStatusCacheTest {

    @Mock
    private ApiStatusRepository apiStatusRepository;

    private ApiStatusCache apiStatusCache;

    @BeforeEach
    void setUp() {
        apiStatusCache = new ApiStatusCache(apiStatusRepository);
    }

    @Test
    void testGetAllApiStatusesCachesResults() {
        List<ApiStatus> mockStatuses = List.of(
                ApiStatus.builder()
                        .apiName("SMHI")
                        .isActive(true)
                        .build(),
                ApiStatus.builder()
                        .apiName("YR")
                        .isActive(true)
                        .build());

        when(apiStatusRepository.findAll()).thenReturn(mockStatuses);

        // First call should fetch from the repository
        List<ApiStatus> result1 = apiStatusCache.getAllApiStatuses();
        assertEquals(mockStatuses, result1);
        verify(apiStatusRepository, times(1)).findAll();

        // Second call should use the cache (repository not called again)
        List<ApiStatus> result2 = apiStatusCache.getAllApiStatuses();
        assertEquals(mockStatuses, result2);
        verify(apiStatusRepository, times(1)).findAll();
    }

    @Test
    void testGetApiStatusReturnsCorrectApi() {
        List<ApiStatus> mockStatuses = List.of(
                ApiStatus.builder()
                        .apiName("SMHI")
                        .isActive(true)
                        .build(),
                ApiStatus.builder()
                        .apiName("YR")
                        .isActive(false)
                        .build());

        when(apiStatusRepository.findAll()).thenReturn(mockStatuses);

        ApiStatus smhiStatus = apiStatusCache.getApiStatus("SMHI");
        assertNotNull(smhiStatus);
        assertTrue(smhiStatus.isActive());
        assertEquals("SMHI", smhiStatus.getApiName());

        ApiStatus yrStatus = apiStatusCache.getApiStatus("YR");
        assertNotNull(yrStatus);
        assertFalse(yrStatus.isActive());

        ApiStatus nonExistentStatus = apiStatusCache.getApiStatus("FMI");
        assertNull(nonExistentStatus);
    }

    @Test
    void testInvalidateCacheClearsCache() {
        List<ApiStatus> mockStatuses = List.of(
                ApiStatus.builder()
                        .apiName("SMHI")
                        .isActive(true)
                        .build(),
                ApiStatus.builder()
                        .apiName("YR")
                        .isActive(false)
                        .build());

        when(apiStatusRepository.findAll()).thenReturn(mockStatuses);

        apiStatusCache.getAllApiStatuses();
        verify(apiStatusRepository, times(1)).findAll();

        apiStatusCache.invalidateCache();

        apiStatusCache.getAllApiStatuses();
        verify(apiStatusRepository, times(2)).findAll();
    }

    @Test
    void testGetApiStatusMap() {
        List<ApiStatus> mockStatuses = List.of(
                ApiStatus.builder()
                        .apiName("SMHI")
                        .isActive(true)
                        .build(),
                ApiStatus.builder()
                        .apiName("YR")
                        .isActive(false)
                        .build());

        when(apiStatusRepository.findAll()).thenReturn(mockStatuses);

        Map<String, Boolean> statusMap = apiStatusCache.getApiStatus();
        assertEquals(2, statusMap.size());
        assertTrue(statusMap.get("SMHI"));
        assertFalse(statusMap.get("YR"));
    }
}
