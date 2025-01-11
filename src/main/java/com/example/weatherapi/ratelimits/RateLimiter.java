package com.example.weatherapi.ratelimits;

import com.example.weatherapi.exceptions.RateLimitExceededException;
import io.github.bucket4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public abstract class RateLimiter {

    private final Logger LOG = LoggerFactory.getLogger(RateLimiter.class);

    private final Bucket perRequestBucket;
    private final Bucket burstBucket;
    private final Bucket dailyBucket;

    private final TimeMeter timeMeter;

    private final long burstCapacity;
    private final long dailyCapacity;
    private final String burstRefillInterval;
    private final String api;

    protected RateLimiter(String api, Bandwidth perRequestLimit, Bandwidth burstLimit, Bandwidth dailyLimit) {
        this(api, TimeMeter.SYSTEM_MILLISECONDS, perRequestLimit, burstLimit, dailyLimit);
    }

    protected RateLimiter(String api, TimeMeter timeMeter, Bandwidth perRequestLimit, Bandwidth burstLimit, Bandwidth dailyLimit) {
        this.api = api;
        this.timeMeter = timeMeter;
        this.perRequestBucket = Bucket.builder().addLimit(perRequestLimit).withCustomTimePrecision(timeMeter).build();
        this.burstBucket = Bucket.builder().addLimit(burstLimit).withCustomTimePrecision(timeMeter).build();
        this.dailyBucket = Bucket.builder().addLimit(dailyLimit).withCustomTimePrecision(timeMeter).build();
        this.burstCapacity = burstLimit.getCapacity();
        this.dailyCapacity = dailyLimit.getCapacity();
        this.burstRefillInterval = formatDuration(burstLimit.getRefillPeriodNanos());
    }

    public static Bandwidth createRequestBandwidth(long timePerRequestMs) {
        return Bandwidth.builder()
                .capacity(1)
                .refillIntervally(1, Duration.ofMillis(timePerRequestMs))
                .initialTokens(1)
                .build();
    }

    public static Bandwidth createBurstBandwidth(long burstCapacity) {
        return Bandwidth.builder()
                .capacity(burstCapacity)
                .refillIntervally(burstCapacity, Duration.ofMinutes(5))
                .initialTokens(burstCapacity)
                .build();
    }

    public static Bandwidth createDailyBandwidth(long dailyCapacity) {
        return Bandwidth.builder()
                .capacity(dailyCapacity)
                .refillIntervally(dailyCapacity, Duration.ofDays(1))
                .initialTokens(dailyCapacity)
                .build();
    }

    public void acquire() throws InterruptedException {
        long startTime = System.currentTimeMillis();

        perRequestBucket.asBlocking().consume(1);

        // Check burst limit
        if (!burstBucket.tryConsume(1)) {
            throw new RateLimitExceededException(
                    "Rate limit exceeded for " + api +
                    ". The " + api + " API allows up to " + burstCapacity +
                            " requests per " + burstRefillInterval + ". Please try again in " + formatDuration(getTimeToWait(burstBucket, timeMeter)) + "."
            );
        }

        // Check daily limit
        if (!dailyBucket.tryConsume(1)) {
            throw new RateLimitExceededException(
                    "Rate limit exceeded for " + api +
                            ". The " + api + " API allows up to " + dailyCapacity +
                            " requests per day. Please try again in " + formatDuration(getTimeToWait(dailyBucket, timeMeter)) + "."
            );
        }

        LOG.debug("Rate limit check for {} took {} ms", api, System.currentTimeMillis() - startTime);
    }

    private long getTimeToWait(Bucket bucket, TimeMeter timeMeter) {
        return bucket.asVerbose().getAvailableTokens().getState()
                .calculateFullRefillingTime(timeMeter.currentTimeNanos());
    }

    private String formatDuration(long nanos) {
        long hours = TimeUnit.NANOSECONDS.toHours(nanos);
        if (hours > 0) {
            long minutes = TimeUnit.NANOSECONDS.toMinutes(nanos) % 60;
            return minutes == 0 ? hours + " hours" : hours + " hours and " + minutes + " minutes";
        }
        long minutes = TimeUnit.NANOSECONDS.toMinutes(nanos);
        if (minutes > 0) {
            long seconds = TimeUnit.NANOSECONDS.toSeconds(nanos) % 60;
            return seconds == 0 ? minutes + " minutes" : minutes + " minutes and " + seconds + " seconds";
        }
        return TimeUnit.NANOSECONDS.toSeconds(nanos) + " seconds";
    }
}
