package com.example.weatherapi.ratelimits;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.TimeMeter;
import org.springframework.stereotype.Component;

import java.time.Duration;

// https://en.ilmatieteenlaitos.fi/open-data-manual-fmi-wfs-services -- Terms and Condition request limitations.
// FMI API rate limiter
// 600 requests per 5 minutes
// 10,000 requests per day
@Component
public class FmiRateLimiter extends RateLimiter {

    public FmiRateLimiter() {
        super("Fmi",
                Bandwidth.builder().capacity(1).refillIntervally(1, Duration.ofMillis(200)).initialTokens(1).build(),
                Bandwidth.builder().capacity(600).refillIntervally(600, Duration.ofMinutes(5)).initialTokens(600).build(),
                Bandwidth.builder().capacity(10000).refillIntervally(10000, Duration.ofDays(1)).initialTokens(10000).build()
        );
    }
}
