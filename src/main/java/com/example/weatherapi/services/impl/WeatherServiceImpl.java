package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.FmiApi;
import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.api.YrApi;
import com.example.weatherapi.cache.ApiStatusCache;
import com.example.weatherapi.cache.CacheDB;
import com.example.weatherapi.cache.MemoryCacheUtils;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.ApiStatus;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.exceptions.InvalidApiUsageException;
import com.example.weatherapi.exceptions.MultipleRateLimitExceededException;
import com.example.weatherapi.exceptions.RateLimitExceededException;
import com.example.weatherapi.exceptions.WeatherNotFilledException;
import com.example.weatherapi.services.CityService;
import com.example.weatherapi.services.WeatherService;
import com.example.weatherapi.util.DataStructures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static com.example.weatherapi.util.CityMapper.toModel;
import static com.example.weatherapi.util.SunriseUtil.getSunriseSunset;
import static com.example.weatherapi.util.WeatherValidation.validateApis;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final CityService cityService;
    private final FmiApi fmiApi;
    private final SmhiApi smhiApi;
    private final YrApi yrApi;
    private final CacheDB cacheDB;
    private final Logger log;
    private final MemoryCacheUtils memoryCacheUtils;
    private final ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<>();
    private final ApiStatusCache apiStatusCache;

    private static final String TEMPERATURE = "temperature";
    private static final String WIND_SPEED = "windSpeed";
    private static final String PRECIPITATION = "precipitation";
    private static final String HUMIDITY = "humidity";

    public static final String API_SMHI = "SMHI";
    public static final String API_YR = "YR";
    public static final String API_FMI = "FMI";
    List<String> allAvailableApis = List.of(API_SMHI, API_YR, API_FMI);

    private static final float INVALID_VALUE = -99f;

    @Autowired
    public WeatherServiceImpl(CityService cityService,
                              SmhiApi smhiApi,
                              YrApi yrApi,
                              FmiApi fmiApi,
                              CacheDB cacheDB,
                              MemoryCacheUtils memoryCacheUtils,
                              ApiStatusCache apiStatusCache
    ) {
        this.cityService = cityService;
        this.fmiApi = fmiApi;
        this.smhiApi = smhiApi;
        this.yrApi = yrApi;
        this.cacheDB = cacheDB;
        this.memoryCacheUtils = memoryCacheUtils;
        this.apiStatusCache = apiStatusCache;
        this.log = LoggerFactory.getLogger(WeatherServiceImpl.class);
    }

    @Override
    public ResponseEntity<Weather> fetchWeatherMergedResponse(String cityName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(getWeatherMerged(cityName.toLowerCase()), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Weather> fetchWeatherMergedCustomApisResponse(String cityName, List<String> enabledApis) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(getWeatherMergedCustomApis(cityName.toLowerCase(), enabledApis), headers, HttpStatus.OK);
    }

    public Weather getWeatherMerged(String cityName) {

        List<String> enabledApis = apiStatusCache.getAllApiStatuses().stream()
                .filter(ApiStatus::isActive)
                .map(ApiStatus::getApiName)
                .sorted()
                .toList();

        String key;
        if (isAllApisEnabled(enabledApis)) {
            key = cityName.toLowerCase() + "merged";
        } else {
            key = getKey(cityName, enabledApis);
        }

        Weather weatherFromCache = memoryCacheUtils.getWeatherFromCache(key, cityName, enabledApis);
        if(weatherFromCache != null) {
            return weatherFromCache;
        }

        log.debug("Thread attempting to acquire lock for City: {}", cityName);

        Lock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();

        try {
            log.debug("Thread acquired lock for City: {} with Merged APIs: {}", cityName, enabledApis);
            weatherFromCache = memoryCacheUtils.getWeatherFromCache(key, cityName, enabledApis);
            if(weatherFromCache != null) {
                return weatherFromCache;
            }

            City city = toModel(cityService.getCityByName(cityName));

            return processAndCacheWeather(enabledApis, key, city);
        } finally {
            lock.unlock();
            log.debug("Thread released lock for City: {} with Merged APIs: {}", cityName, enabledApis);
        }
    }

    public Weather getWeatherMergedCustomApis(String cityName, List<String> enabledApis) {
        if(enabledApis == null || enabledApis.isEmpty()) {
            return getWeatherMerged(cityName);
        }

        enabledApis = enabledApis.stream().map(String::toUpperCase).sorted().toList();
        String key = getKey(cityName, enabledApis);

        Weather weatherFromCache = memoryCacheUtils.getWeatherFromCache(key, cityName, enabledApis);
        if(weatherFromCache != null) {
            return weatherFromCache;
        }

        List<String> allActiveApis = validateApis(enabledApis, apiStatusCache);

        if (new HashSet<>(allActiveApis).equals(new HashSet<>(enabledApis))){
            return getWeatherMerged(cityName);
        }

        log.debug("Thread attempting to acquire lock for City: {} with Custom APIs: {}", cityName, enabledApis);

        Lock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();

        try {
            log.debug("Thread acquired lock for City: {} with Custom APIs: {}", cityName, enabledApis);
            weatherFromCache = memoryCacheUtils.getWeatherFromCache(key, cityName, enabledApis);
            if(weatherFromCache != null) {
                return weatherFromCache;
            }

            City city = toModel(cityService.getCityByName(cityName));

            if(enabledApis.size() == 1){
                return getWeatherSingleApi(city, enabledApis.get(0));
            }

            return processAndCacheWeather(enabledApis, key, city);
        } finally {
            lock.unlock();
            log.debug("Thread released lock for City: {} with Custom APIs: {}", cityName, enabledApis);
        }
    }

    private Weather processAndCacheWeather(List<String> enabledApis, String key, City city) {

        DataStructures dataStructures = new DataStructures();

        Weather mergedWeather = fetchWeatherData(city, enabledApis, dataStructures);

        mergedWeather.getWeatherData().entrySet().removeIf(entry -> entry.getValue().getWeatherCode() == -1);

        if (dataStructures.successfulApis().size() > 1) {
            cacheDB.saveDB(mergedWeather, dataStructures.successfulApis());
        }

        getSunriseSunset(mergedWeather);

        memoryCacheUtils.putWeatherInCache(key, mergedWeather);

        return mergedWeather;
    }

    private Weather getWeatherSingleApi(City city, String s) {
        if(s.equalsIgnoreCase(API_SMHI)){
            return smhiApi.getWeatherSmhi(city.getLon(), city.getLat(), city);
        } else if(s.equalsIgnoreCase(API_YR)){
            return yrApi.getWeatherYr(city.getLon(), city.getLat(), city);
        }
        throw new InvalidApiUsageException("Unsupported API:" + s);
    }

    private Weather fetchWeatherData(City city, List<String> enabledApis, DataStructures dataStructures) throws WeatherNotFilledException {

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<ZonedDateTime> apiTimestamps = Collections.synchronizedList(new ArrayList<>());

        Map<String, Throwable> failedApis = new HashMap<>();

        if (enabledApis.contains(API_SMHI)) {
            CompletableFuture<Void> smhi = fetchAndProcessWeatherData(
                    API_SMHI,
                    smhiApi.fetchWeatherSmhiAsync(city).thenApply(weather -> {
                        apiTimestamps.add(weather.getTimestamp());
                        return weather;
                    }),
                    dataStructures.mergedWeatherData(),
                    dataStructures.updateCountMap(),
                    dataStructures.successfulApis(),
                    failedApis
            );
            futures.add(smhi);
        }

        if (enabledApis.contains(API_YR)) {
            CompletableFuture<Void> yr = fetchAndProcessWeatherData(
                    API_YR,
                    yrApi.fetchWeatherYrAsync(city).thenApply(weather -> {
                        apiTimestamps.add(weather.getTimestamp());
                        return weather;
                    }),
                    dataStructures.mergedWeatherData(),
                    dataStructures.updateCountMap(),
                    dataStructures.successfulApis(),
                    failedApis
            );
            futures.add(yr);
        }

        if (enabledApis.contains(API_FMI)) {
            CompletableFuture<Weather> fmiWeatherFuture = fmiApi.fetchWeatherFmiAsync(city);
            CompletableFuture<Void> fmi = CompletableFuture.anyOf(
                    futures.toArray(new CompletableFuture<?>[0])
            ).thenCompose(v -> fetchAndProcessWeatherData(
                    API_FMI,
                    fmiWeatherFuture.thenApply(weather -> {
                        apiTimestamps.add(weather.getTimestamp());
                        return weather;
                    }),
                    dataStructures.mergedWeatherData(),
                    dataStructures.updateCountMap(),
                    dataStructures.successfulApis(),
                    failedApis
                    )
            );
            futures.add(fmi);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        if(dataStructures.mergedWeatherData().isEmpty()) {
            handleApiFailures(failedApis, enabledApis);
        }

        if (dataStructures.mergedWeatherData().isEmpty()) {
            if (enabledApis.size() == 1 && failedApis.size() == 1 && failedApis.containsKey(enabledApis.get(0))) {
                // Single API request and it failed; rethrow the original exception
                Throwable originalException = failedApis.values().iterator().next();
                if (originalException instanceof RateLimitExceededException) {
                    throw (RateLimitExceededException) originalException;
                } else {
                    throw new WeatherNotFilledException("Failed to fetch weather data for " + enabledApis + ".");
                }
            } else if (enabledApis.size() == failedApis.size() && failedApis.values().stream().allMatch(e -> e instanceof RateLimitExceededException)) {
                // All APIs hit rate limits
                throw new RateLimitExceededException(
                        "All requested APIs hit rate limits: " + failedApis.keySet()
                );
            } else {
                // General failure
                throw new WeatherNotFilledException(
                        "Could not connect to any weather API. Failed APIs: " + failedApis.keySet() +
                                ". API Status: " + apiStatusCache.getApiStatus()
                );
            }
        }

        calculateAverages(dataStructures.mergedWeatherData(), dataStructures.updateCountMap());
        setScaleWeatherData(dataStructures.mergedWeatherData());

        ZonedDateTime oldestTimestamp = apiTimestamps.stream()
                .min(ZonedDateTime::compareTo)
                .orElse(ZonedDateTime.now(ZoneId.of("UTC")));

        return Weather.builder()
                .message(createMessage(city, dataStructures.successfulApis()))
                .weatherData(dataStructures.mergedWeatherData())
                .timestamp(oldestTimestamp)
                .city(city)
                .build();
    }

    private CompletableFuture<Void> fetchAndProcessWeatherData(
            String apiName,
            CompletableFuture<Weather> weatherFuture,
            Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData,
            Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap,
            List<String> successfulApis,
            Map<String, Throwable> failedApis) {

        return CompletableFuture.runAsync(() -> weatherFuture
                .exceptionally(e -> {
                    failedApis.put(apiName, e.getCause());
                    return null;
                })
                .thenAccept(weather -> {
                    if (weather != null) {
                        mergeWeatherDataIntoMergedData(
                                weather.getWeatherData(),
                                apiName,
                                mergedWeatherData,
                                updateCountMap
                        );
                        successfulApis.add(apiName);
                    }
                }).join());
    }

    private synchronized void mergeWeatherDataIntoMergedData(
            Map<ZonedDateTime, Weather.WeatherData> newData,
            String api,
            Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData,
            Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap) {

        AtomicInteger newDataCount = new AtomicInteger();
        AtomicInteger mergedDataCount = new AtomicInteger();

        newData.forEach((key, newDataItem) -> {
            Weather.WeatherData existingData = mergedWeatherData.get(key);
            if (existingData != null) {
                updateDataField(newDataItem.getTemperature(),
                        existingData::setTemperature,
                        existingData.getTemperature(),
                        key,
                        TEMPERATURE,
                        updateCountMap);
                updateDataField(newDataItem.getPrecipitation(),
                        existingData::setPrecipitation,
                        existingData.getPrecipitation(),
                        key,
                        PRECIPITATION,
                        updateCountMap);
                updateDataField(newDataItem.getWindSpeed(),
                        existingData::setWindSpeed,
                        existingData.getWindSpeed(),
                        key,
                        WIND_SPEED,
                        updateCountMap);
                updateDataField(newDataItem.getHumidity(),
                        existingData::setHumidity,
                        existingData.getHumidity(),
                        key,
                        HUMIDITY,
                        updateCountMap);

                if (newDataItem.getWindDirection() != INVALID_VALUE) {
                    existingData.setWindDirection(
                            getAvgWindDirection(
                                    existingData.getWindDirection(),
                                    newDataItem.getWindDirection()
                            )
                    );
                }
                existingData.setWeatherCode(determineWeatherCode(api,existingData, newDataItem));
                mergedDataCount.incrementAndGet();
            } else if (!api.equals(API_FMI)) {
                mergedWeatherData.put(key, newDataItem);
                updateCount(key, TEMPERATURE, updateCountMap);
                updateCount(key, WIND_SPEED, updateCountMap);
                updateCount(key, PRECIPITATION, updateCountMap);
                updateCount(key, HUMIDITY, updateCountMap);
                newDataCount.incrementAndGet();
            }
        });
        log.info("{} weather data from {}",
                (newDataCount.get() > 0 && mergedDataCount.get() == 0) ? "Added" : "Merged",  api);
    }

    private void updateDataField(
            float newDataValue,
            Consumer<Float> setter,
            float existingValue,
            ZonedDateTime key,
            String fieldName,
            Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap
    ) {
        if (newDataValue != INVALID_VALUE) {
            setter.accept(existingValue + newDataValue);
            updateCount(key, fieldName, updateCountMap);
        }
    }

    private int determineWeatherCode(
            String api, Weather.WeatherData existingData, Weather.WeatherData newDataItem) {
        if (api.equals(API_SMHI)) {
            return newDataItem.getWeatherCode();
        } else {
            return existingData.getWeatherCode() > -1 ? existingData.getWeatherCode() : newDataItem.getWeatherCode();
        }
    }

    private void updateCount(ZonedDateTime key, String attribute, Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap) {
        updateCountMap.computeIfAbsent(attribute, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(key, k -> new ConcurrentHashMap<>())
                .merge(attribute, 1, Integer::sum);
    }

    private int getUpdateCount(ZonedDateTime key, String attribute, Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap) {
        return updateCountMap.getOrDefault(attribute, Collections.emptyMap())
                .getOrDefault(key, Collections.emptyMap())
                .getOrDefault(attribute, 1);
    }

    private void calculateAverages(Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData, Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap) {
        for (Map.Entry<ZonedDateTime, Weather.WeatherData> entry : mergedWeatherData.entrySet()) {
            ZonedDateTime key = entry.getKey();
            Weather.WeatherData data = entry.getValue();

            data.setTemperature(data.getTemperature() / getUpdateCount(key, TEMPERATURE, updateCountMap));
            data.setWindSpeed(data.getWindSpeed() / getUpdateCount(key, WIND_SPEED, updateCountMap));
            data.setPrecipitation(data.getPrecipitation() / getUpdateCount(key, PRECIPITATION, updateCountMap));
            data.setHumidity(data.getHumidity() / getUpdateCount(key, HUMIDITY, updateCountMap));

        }
    }

    private float getAvgWindDirection(float existingWindDirection, float newDataWindDirection) {
        // Convert wind directions to Cartesian coordinates
        double existingX = Math.cos(Math.toRadians(existingWindDirection));
        double existingY = Math.sin(Math.toRadians(existingWindDirection));
        double newX = Math.cos(Math.toRadians(newDataWindDirection));
        double newY = Math.sin(Math.toRadians(newDataWindDirection));

        // Sum up Cartesian coordinates
        double sumX = existingX + newX;
        double sumY = existingY + newY;

        // Convert back to polar coordinates
        float avgWindDirection = (float) Math.toDegrees(Math.atan2(sumY, sumX));
        if (avgWindDirection < 0) {
            avgWindDirection += 360;
        }
        return avgWindDirection;
    }


        private void setScaleWeatherData(Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData){
        for (Map.Entry<ZonedDateTime, Weather.WeatherData> entry : mergedWeatherData.entrySet()) {
            Weather.WeatherData weatherData = entry.getValue();
            weatherData.setTemperature(BigDecimal.valueOf(weatherData.getTemperature()).setScale(1, RoundingMode.HALF_UP).floatValue());
            weatherData.setWindDirection(BigDecimal.valueOf(weatherData.getWindDirection()).setScale(1, RoundingMode.HALF_UP).floatValue());
            weatherData.setWindSpeed(BigDecimal.valueOf(weatherData.getWindSpeed()).setScale(1, RoundingMode.HALF_UP).floatValue());
            weatherData.setPrecipitation(BigDecimal.valueOf(weatherData.getPrecipitation()).setScale(1, RoundingMode.HALF_UP).floatValue());
            weatherData.setHumidity(BigDecimal.valueOf(weatherData.getHumidity()).setScale(1, RoundingMode.HALF_UP).floatValue());
        }
    }

    private String createMessage(City city, List<String> successfulApis){
        List<String> sortedApis = new ArrayList<>(successfulApis);
        Collections.sort(sortedApis);

        StringBuilder messageBuilder = new StringBuilder("Merged weather for ")
                .append(city.getName())
                .append(" from ");

        if (sortedApis.size() == 1) {
            messageBuilder.append(sortedApis.get(0));
        } else {
            messageBuilder.append(String.join(", ", sortedApis.subList(0, sortedApis.size() - 1)))
                    .append(" and ")
                    .append(sortedApis.get(sortedApis.size() - 1));
        }

        return messageBuilder.toString();
    }

    private boolean isAllApisEnabled(List<String> allActiveApis) {
        return new HashSet<>(allActiveApis).equals(new HashSet<>(allAvailableApis));
    }

    private String getKey(String cityName, List<String> enabledApis){
        return cityName.toLowerCase() + String.join("_", enabledApis);
    }

    private void handleApiFailures(Map<String, Throwable> failedApis, List<String> enabledApis) {
            if (enabledApis.size() == 1 && failedApis.size() == 1 && failedApis.containsKey(enabledApis.get(0))) {
                Throwable originalException = failedApis.values().iterator().next();
                if (originalException instanceof RateLimitExceededException) {
                    throw (RateLimitExceededException) originalException;
                } else {
                    throw new WeatherNotFilledException("Failed to fetch weather data for " + enabledApis.get(0) + ".");
                }
            } else if (enabledApis.size() == failedApis.size() && failedApis.values().stream().allMatch(e -> e instanceof RateLimitExceededException)) {
                throw new MultipleRateLimitExceededException(failedApis);
            } else {
                throw new WeatherNotFilledException(
                        "Could not connect to any weather API. Failed APIs: " + failedApis.keySet() +
                                ". API Status: " + apiStatusCache.getApiStatus()
                );
            }
    }

}