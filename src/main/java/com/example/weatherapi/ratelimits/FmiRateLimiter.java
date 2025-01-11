package com.example.weatherapi.ratelimits;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// https://en.ilmatieteenlaitos.fi/open-data-manual-fmi-wfs-services -- Terms and Condition request limitations.
// FMI API rate limiter
// 600 requests per 5 minutes
// 10,000 requests per day

@Component
public class FmiRateLimiter extends RateLimiter {

    public FmiRateLimiter(
            @Value("${fmi.rate-limiter.minimum-request-interval-ms:200}") long timePerRequestMs,
            @Value("${fmi.rate-limiter.burst-capacity:600}") long burstCapacity,
            @Value("${fmi.rate-limiter.daily-capacity:10000}") long dailyCapacity) {
        super("Fmi",
                createRequestBandwidth(timePerRequestMs),
                createBurstBandwidth(burstCapacity),
                createDailyBandwidth(dailyCapacity)
        );
    }
}
