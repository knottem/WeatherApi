package com.example.weatherapi.ratelimits;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.TimeMeter;

public class TestRateLimiter extends RateLimiter {
    protected TestRateLimiter(String api, TimeMeter timeMeter, Bandwidth perRequestLimit, Bandwidth shortTermLimit, Bandwidth dailyLimit) {
        super(api, timeMeter, perRequestLimit, shortTermLimit, dailyLimit);
    }
}
