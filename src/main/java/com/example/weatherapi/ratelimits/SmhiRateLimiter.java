package com.example.weatherapi.ratelimits;

import io.github.bucket4j.Bandwidth;
import org.springframework.stereotype.Component;

import java.time.Duration;

// https://www.smhi.se/en/services/open-data/conditions-of-use-1.33347
// Smhi open data API rate limiter
// Smhi does not set a rate limit in their terms and conditions, but we set a rate limit just to be safe.
@Component
public class SmhiRateLimiter extends RateLimiter {

    public SmhiRateLimiter() {
        super("Smhi",
                Bandwidth.builder().capacity(1).refillIntervally(1, Duration.ofMillis(200)).initialTokens(1).build(),
                Bandwidth.builder().capacity(600).refillIntervally(600, Duration.ofMinutes(5)).initialTokens(600).build(),
                Bandwidth.builder().capacity(10000).refillIntervally(10000, Duration.ofDays(1)).initialTokens(10000).build()
        );
    }
}
