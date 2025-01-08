package com.example.weatherapi.api.ratelimits;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
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
                TimeMeter.SYSTEM_MILLISECONDS,
                Bandwidth.classic(1, Refill.intervally(1, Duration.ofMillis(200))).withInitialTokens(1),
                Bandwidth.classic(600, Refill.intervally(600, Duration.ofMinutes(5))).withInitialTokens(600),
                Bandwidth.classic(10_000, Refill.intervally(10_000, Duration.ofDays(1))).withInitialTokens(10_000)
        );
    }
}
