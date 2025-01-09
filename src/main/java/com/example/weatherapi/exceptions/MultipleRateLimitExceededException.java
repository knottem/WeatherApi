package com.example.weatherapi.exceptions;

import lombok.Getter;

import java.util.Map;

@Getter
public class MultipleRateLimitExceededException extends RateLimitExceededException {

    public MultipleRateLimitExceededException(Map<String, Throwable> failedApis) {
        super("Rate limit exceeded for multiple APIs " + failedApis.keySet() + ". Please try again later or with different APIs.");

    }

}
