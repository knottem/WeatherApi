package com.example.weatherapi.api.ratelimits;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.TimeMeter;

public abstract class RateLimiter {

    private final Bucket perRequestBucket;
    private final Bucket shortTermBucket;
    private final Bucket dailyBucket;

    private final long shortTermLimit;
    private final long dailyLimit;
    private final String api;

    protected RateLimiter(String api, TimeMeter timeMeter, Bandwidth perRequestLimit, Bandwidth shortTermLimit, Bandwidth dailyLimit) {
        this.api = api;
        this.perRequestBucket = createBucket(perRequestLimit, timeMeter);
        this.shortTermBucket = createBucket(shortTermLimit, timeMeter);
        this.dailyBucket = createBucket(dailyLimit, timeMeter);
        this.shortTermLimit = shortTermLimit.getCapacity();
        this.dailyLimit = dailyLimit.getCapacity();
    }

    private Bucket createBucket(Bandwidth bandwidth, TimeMeter timeMeter) {
        return Bucket.builder()
                .addLimit(bandwidth)
                .withCustomTimePrecision(timeMeter)
                .build();
    }

    public void acquire() throws InterruptedException {
        // Enforce per-request delay
        perRequestBucket.asBlocking().consume(1);

        // Check short-term limit
        if (!shortTermBucket.tryConsume(1)) {
            throw new RuntimeException("Short-term rate limit exceeded for " + api + " (" + shortTermLimit + " requests per 5 minutes)");
        }

        // Check daily limit
        if (!dailyBucket.tryConsume(1)) {
            throw new RuntimeException("Daily rate limit exceeded for " + api + " (" + dailyLimit +  " requests per day)");
        }
    }
}
