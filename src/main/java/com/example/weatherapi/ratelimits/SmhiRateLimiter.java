package com.example.weatherapi.ratelimits;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// https://www.smhi.se/en/services/open-data/conditions-of-use-1.33347
// Smhi open data API rate limiter
// Smhi does not set a rate limit in their terms and conditions, but we set a rate limit just to be safe.
@Component
public class SmhiRateLimiter extends RateLimiter {

    public SmhiRateLimiter(
            @Value("${smhi.rate-limiter.minimum-request-interval-ms:200}") long timePerRequestMs,
            @Value("${smhi.rate-limiter.burst-capacity:1000}") long burstCapacity,
            @Value("${smhi.rate-limiter.daily-capacity:10000}") long dailyCapacity
    ) {
        super("Smhi",
                createRequestBandwidth(timePerRequestMs),
                createBurstBandwidth(burstCapacity),
                createDailyBandwidth(dailyCapacity)
        );
    }
}
