package com.example.weatherapi.ratelimits;

import io.github.bucket4j.Bandwidth;
import org.springframework.stereotype.Component;

import java.time.Duration;

// https://developer.yr.no/doc/TermsOfService/
// Yr API rate limiter
// Yr only requires Anything over 20 requests/seconds per application(total) to be approved by them.
@Component
public class YrRateLimiter extends RateLimiter {

    public YrRateLimiter() {
        super("Yr",
                Bandwidth.builder().capacity(1).refillIntervally(1, Duration.ofMillis(200)).initialTokens(1).build(),
                Bandwidth.builder().capacity(600).refillIntervally(600, Duration.ofMinutes(5)).initialTokens(600).build(),
                Bandwidth.builder().capacity(10000).refillIntervally(10000, Duration.ofDays(1)).initialTokens(10000).build()
        );
    }
}
