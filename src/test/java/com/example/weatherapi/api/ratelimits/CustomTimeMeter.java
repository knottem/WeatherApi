package com.example.weatherapi.api.ratelimits;

import io.github.bucket4j.TimeMeter;

public class CustomTimeMeter implements TimeMeter {

    private long currentTime = 0;

    public CustomTimeMeter() {}

    @Override
    public long currentTimeNanos() {
        return currentTime;
    }

    @Override
    public boolean isWallClockBased() {
        return false;
    }

    public void addMinutes(long minutes) {
        currentTime += minutes * 60 * 1_000_000_000;
    }

    public void addMillis(long millis) {
        currentTime += millis * 1_000_000;
    }

    public long getCurrentTimeInMilliSeconds() {
        return currentTime / 1_000_000;
    }
}
