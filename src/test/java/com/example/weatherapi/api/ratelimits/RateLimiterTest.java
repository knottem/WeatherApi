package com.example.weatherapi.api.ratelimits;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    CustomTimeMeter customTimeMeter;

    @BeforeEach
    void setUp() {
        customTimeMeter = new CustomTimeMeter();
    }


    @Test
    void testPerRequestRateLimit() throws InterruptedException {
        RateLimiter rateLimiter = new TestRateLimiter(
                "Test",
                customTimeMeter,
                Bandwidth.classic(1, Refill.intervally(1, Duration.ofMillis(200))).withInitialTokens(1),
                Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(5))).withInitialTokens(100),
                Bandwidth.classic(100, Refill.intervally(100, Duration.ofDays(1))).withInitialTokens(100)
        );
        long startTime = customTimeMeter.getCurrentTimeInMilliSeconds();
        int requestCount = 10;
        for (int i = 0; i < requestCount; i++) {
            rateLimiter.acquire();
            customTimeMeter.addMillis(200);
        }
        assertThat(customTimeMeter.getCurrentTimeInMilliSeconds()).isGreaterThanOrEqualTo(startTime + 200 * requestCount);
    }

    @Test
    void testShortTermLimitWithCustomTimeMeter() throws InterruptedException {
        RateLimiter rateLimiter = new TestRateLimiter(
                "Test",
                customTimeMeter,
                Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofMinutes(5))).withInitialTokens(1000),
                Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(5))).withInitialTokens(10),
                Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofDays(1))).withInitialTokens(1000)
        );

        for (int i = 0; i < 10; i++) {
            rateLimiter.acquire();
        }

        RuntimeException exception = assertThrows(RuntimeException.class, rateLimiter::acquire);
        assertEquals("Short-term rate limit exceeded for Test (10 requests per 5 minutes)", exception.getMessage());
        customTimeMeter.addMinutes(5);
        assertDoesNotThrow(rateLimiter::acquire);
    }

    @Test
    void testDailyViewLimitWithCustomTimeMeter() throws InterruptedException {
        RateLimiter rateLimiter = new TestRateLimiter(
                "Test",
                customTimeMeter,
                Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofMinutes(5))).withInitialTokens(1000),
                Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(5))).withInitialTokens(10),
                Bandwidth.classic(100, Refill.intervally(100, Duration.ofDays(1))).withInitialTokens(100)
        );

        // Consume all tokens in the daily view bucket (100 tokens)
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Consume 10 tokens from the 5-minute bucket
                rateLimiter.acquire();
            }
            assertThrows(RuntimeException.class, rateLimiter::acquire);
            customTimeMeter.addMinutes(5);
        }

        RuntimeException exception = assertThrows(RuntimeException.class, rateLimiter::acquire);
        assertEquals("Daily rate limit exceeded for Test (100 requests per day)", exception.getMessage());

        customTimeMeter.addMinutes(1440);

        assertDoesNotThrow(rateLimiter::acquire);
    }
}