package com.example.weatherapi.ratelimits;

import com.example.weatherapi.exceptions.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
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
                Bandwidth.builder().capacity(1).refillIntervally(1, Duration.ofMillis(200)).initialTokens(1).build(),
                Bandwidth.builder().capacity(100).refillIntervally(100, Duration.ofMinutes(5)).initialTokens(100).build(),
                Bandwidth.builder().capacity(100).refillIntervally(100, Duration.ofDays(1)).initialTokens(100).build()
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
    void testBurstLimitEnforcement() throws InterruptedException {
        String api = "Test";
        long burstLimit = 10;

        RateLimiter rateLimiter = createBurst(api, burstLimit);

        for (int i = 0; i < burstLimit; i++) {
            rateLimiter.acquire();
        }

        assertThrows(RateLimitExceededException.class, rateLimiter::acquire);
        customTimeMeter.addMinutes(5);
        assertDoesNotThrow(rateLimiter::acquire);
    }

    @Test
    void testDailyLimitEnforcement() throws InterruptedException {
        String api = "Test";
        long dailyLimit = 100;

        RateLimiter rateLimiter = createDaily(api, dailyLimit);

        for (int i = 0; i < dailyLimit; i++) {
            rateLimiter.acquire();
        }

        assertThrows(RateLimitExceededException.class, rateLimiter::acquire);
        customTimeMeter.addMinutes(1440);
        assertDoesNotThrow(rateLimiter::acquire);
    }

    @Test
    void testRateLimitBurstExceptionMessage() throws InterruptedException {
        String api = "Test";
        String bucket = "Burst";
        long burstLimit = 1;
        RateLimiter rateLimiter = createBurst(api, burstLimit);

        for (int i = 0; i < burstLimit; i++) {
            rateLimiter.acquire();
        }

        RateLimitExceededException exception = assertThrows(RateLimitExceededException.class, rateLimiter::acquire);
        assertEquals(message(api, bucket, burstLimit, 0, 5, 0), exception.getMessage());
        customTimeMeter.addMinutes(1);
        exception = assertThrows(RateLimitExceededException.class, rateLimiter::acquire);
        assertEquals(message(api, bucket, burstLimit, 0, 4, 0), exception.getMessage());
        customTimeMeter.addSeconds(30);
        exception = assertThrows(RateLimitExceededException.class, rateLimiter::acquire);
        assertEquals(message(api, bucket, burstLimit, 0, 3, 30), exception.getMessage());
        customTimeMeter.addSeconds(30);
        customTimeMeter.addMinutes(3);
        assertDoesNotThrow(rateLimiter::acquire);
    }


    @Test
    void testRateLimitDailyExceptionMessage() throws InterruptedException {
        String api = "Test";
        String bucket = "Daily";
        long dailyLimit = 1;

        RateLimiter rateLimiter = createDaily(api, dailyLimit);

        rateLimiter.acquire();
        
        RuntimeException exception = assertThrows(RateLimitExceededException.class, rateLimiter::acquire);
        assertEquals(message(api, bucket, dailyLimit, 24, 0, 0), exception.getMessage());

        customTimeMeter.addMinutes(10);
        exception = assertThrows(RateLimitExceededException.class, rateLimiter::acquire);
        assertEquals(message(api, bucket, dailyLimit, 23, 50, 50), exception.getMessage());
    }


    private RateLimiter createBurst(String api, long burstLimit) {
        return  new TestRateLimiter(
                api,
                customTimeMeter,
                Bandwidth.builder().capacity(1000).refillIntervally(1000, Duration.ofMillis(200)).initialTokens(1000).build(),
                Bandwidth.builder().capacity(burstLimit).refillIntervally(burstLimit, Duration.ofMinutes(5)).initialTokens(burstLimit).build(),
                Bandwidth.builder().capacity(1000).refillIntervally(1000, Duration.ofDays(1)).initialTokens(1000).build()
        );
    }

    private RateLimiter createDaily(String api, long dailyLimit) {
        return new TestRateLimiter(
                api,
                customTimeMeter,
                Bandwidth.builder().capacity(1000).refillIntervally(1000, Duration.ofMillis(200)).initialTokens(1000).build(),
                Bandwidth.builder().capacity(1000).refillIntervally(1000, Duration.ofMinutes(5)).initialTokens(1000).build(),
                Bandwidth.builder().capacity(dailyLimit).refillIntervally(dailyLimit, Duration.ofDays(1)).initialTokens(dailyLimit).build()
        );
    }

    private String message(String api, String bucket, long limit, long hours, long minutes, long seconds) {
        String timePart = hours > 0
                ? (minutes > 0
                ? hours + " hours and " + minutes + " minutes"
                : hours + " hours")
                : (minutes > 0
                ? (seconds > 0
                ? minutes + " minutes and " + seconds + " seconds"
                : minutes + " minutes")
                : seconds + " seconds");
        String bucketType = bucket.equals("Burst") ? "per 5 minutes" : "per day";
        return "Rate limit exceeded for " + api +
                ". The " + api + " API allows up to " + limit +
                " requests " + bucketType + ". Please try again in " + timePart + ".";
    }
}