package com.example.weatherapi.api.ratelimits;

import io.github.bucket4j.TimeMeter;

public class CustomTimeMeter implements TimeMeter {

    private long currentTime;

    public CustomTimeMeter(long startTime) {
        this.currentTime = startTime;
    }

    @Override
    public long currentTimeNanos() {
        return currentTime;
    }

    @Override
    public boolean isWallClockBased() {
        return false;
    }

    public void addMinutes(long minutes) {
        currentTime += minutes * 60 * 1_000_000_000; // Convert minutes to nanoseconds
    }
}
