package com.example.weatherapi.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCache;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


class CacheExpirationTest {

    private CaffeineCache cache;

    private void initializeCache(long expirationMillis) {
        cache = new CaffeineCache("testCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(expirationMillis, TimeUnit.MILLISECONDS)
                        .maximumSize(2)
                        .build());
    }

    @Test
    void testCacheExpiresQuickly() throws InterruptedException {

        initializeCache(200);

        String key = "testKey";
        String value = "testValue";

        cache.put(key, value);
        assertEquals(value, cache.get(key, String.class));

        Thread.sleep(210);

        assertNull(cache.get(key, String.class));
    }

    @Test
    void testCacheEviction() {

        initializeCache(5000);

        String key = "testKey";
        String value = "testValue";
        cache.put(key, value);
        assertEquals(value, cache.get(key, String.class));
        cache.evict(key);
        assertNull(cache.get(key, String.class));
    }

}

