package com.example.weatherapi.api.ratelimits;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import io.github.bucket4j.TimeMeter;
import org.springframework.stereotype.Component;

import java.time.Duration;

// Smhi open data API rate limiter
// https://www.smhi.se/en/services/open-data/conditions-of-use-1.33347
// Smhi does not set a rate limit in their terms and conditions, but we set a rate limit just to be safe.
@Component
public class SmhiRateLimiter extends RateLimiter {

    public SmhiRateLimiter() {
        super("Smhi",
                TimeMeter.SYSTEM_MILLISECONDS,
                Bandwidth.classic(1, Refill.intervally(1, Duration.ofMillis(200))).withInitialTokens(1),
                Bandwidth.classic(600, Refill.intervally(600, Duration.ofMinutes(5))).withInitialTokens(600),
                Bandwidth.classic(10_000, Refill.intervally(10_000, Duration.ofDays(1))).withInitialTokens(10_000)
        );
    }
}
