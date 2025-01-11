package com.example.weatherapi.ratelimits;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// https://developer.yr.no/doc/TermsOfService/
// Yr API rate limiter
// Yr only requires Anything over 20 requests/seconds per application(total) to be approved by them.
@Component
public class YrRateLimiter extends RateLimiter {

    public YrRateLimiter(
            @Value("${yr.rate-limiter.minimum-request-interval-ms:200}") long timePerRequestMs,
            @Value("${yr.rate-limiter.burst-capacity:1000}") long burstCapacity,
            @Value("${yr.rate-limiter.daily-capacity:10000}") long dailyCapacity
    ) {
        super("Yr",
                createRequestBandwidth(timePerRequestMs),
                createBurstBandwidth(burstCapacity),
                createDailyBandwidth(dailyCapacity)
        );
    }
}
