package com.example.weatherapi.api.ratelimits;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FmiRateLimiterTest {

    @Test
    void testPerRequestRateLimit() throws InterruptedException {
        FmiRateLimiter rateLimiter = new FmiRateLimiter();
        long startTime = System.nanoTime();
        int requestCount = 5;
        for (int i = 0; i < requestCount; i++) {
            rateLimiter.acquire();
            System.out.println("Request " + i + " completed at time since start: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
        }
        long endTime = System.nanoTime();
        long elapsedTimeMillis = (endTime - startTime) / 1_000_000;
        long expectedTimeMillis = (requestCount - 1) * 200; // 200ms delay between each request

        assertThat(elapsedTimeMillis).isGreaterThanOrEqualTo(expectedTimeMillis);
    }

    @Test
    void testShortTermLimitWithCustomTimeMeter() throws InterruptedException {
        CustomTimeMeter customTimeMeter = new CustomTimeMeter(0); // Start time at 0
        FmiRateLimiter rateLimiter = new FmiRateLimiter(customTimeMeter, true);

        // Consume all tokens in the short-term bucket
        for (int i = 0; i < 10; i++) {
            rateLimiter.acquire();
        }

        assertThrows(RuntimeException.class, rateLimiter::acquire);

        // Simulate passing 5 minutes to refill the bucket
        customTimeMeter.addMinutes(5);

        assertDoesNotThrow(rateLimiter::acquire);
    }

    @Test
    void testDailyViewLimitWithCustomTimeMeter() throws InterruptedException {
        CustomTimeMeter customTimeMeter = new CustomTimeMeter(0); // Start time at 0
        FmiRateLimiter rateLimiter = new FmiRateLimiter(customTimeMeter, true);

        // Consume all tokens in the daily view bucket (100 tokens)
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Consume 10 tokens from the 5-minute bucket
                rateLimiter.acquire();
            }
            assertThrows(RuntimeException.class, rateLimiter::acquire);
            customTimeMeter.addMinutes(5);
        }

        assertThrows(RuntimeException.class, rateLimiter::acquire);

        customTimeMeter.addMinutes(1440); // Simulate passing 1 day

        assertDoesNotThrow(rateLimiter::acquire);
    }
}