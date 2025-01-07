package com.example.weatherapi.api.ratelimits;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.bucket4j.TimeMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

// https://en.ilmatieteenlaitos.fi/open-data-manual-fmi-wfs-services -- Terms and Condition request limitations.
@Component
public class FmiRateLimiter {

    private final Bucket perRequestBucket;
    private final Bucket shortTermBucket;
    private final Bucket dailyViewBucket;

    private final Logger LOG = LoggerFactory.getLogger(FmiRateLimiter.class);

    public FmiRateLimiter() {
        this(TimeMeter.SYSTEM_MILLISECONDS, false);
    }

    public FmiRateLimiter(TimeMeter timeMeter, boolean isTestMode) {

        Bandwidth shortTermLimit;
        Bandwidth dailyViewLimit;
        Bandwidth perRequestLimit;

        if(isTestMode) {
            shortTermLimit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(5))).withInitialTokens(10);
            dailyViewLimit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofDays(1))).withInitialTokens(100);
            perRequestLimit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMillis(200)));
        } else {
            // 600 requests per 5 minutes
            shortTermLimit = Bandwidth.classic(600, Refill.intervally(600, Duration.ofMinutes(5))).withInitialTokens(600);
            // 10,000 requests per day
            dailyViewLimit = Bandwidth.classic(10_000, Refill.intervally(10_000, Duration.ofDays(1))).withInitialTokens(10_000);
            // 1 requests per 200 milliseconds - Not mentioned in the terms and condition, but added since it is a common rate limit.
            perRequestLimit = Bandwidth.classic(1, Refill.intervally(1, Duration.ofMillis(200)));
        }

        this.shortTermBucket = Bucket.builder().addLimit(shortTermLimit).withCustomTimePrecision(timeMeter).build();
        this.dailyViewBucket = Bucket.builder().addLimit(dailyViewLimit).withCustomTimePrecision(timeMeter).build();
        this.perRequestBucket = Bucket.builder().addLimit(perRequestLimit).withCustomTimePrecision(timeMeter).build();
    }

    public void acquire() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        perRequestBucket.asBlocking().consume(1);

        if (!shortTermBucket.tryConsume(1)) {
            throw new RuntimeException("Short-term rate limit exceeded (600 requests per 5 minutes)");
        }

        if (!dailyViewBucket.tryConsume(1)) {
            throw new RuntimeException("Daily rate limit exceeded (10,000 requests per day)");
        }
        LOG.debug("Rate limit token acquired for FMI API in {} ms", System.currentTimeMillis() - startTime);
    }
}
