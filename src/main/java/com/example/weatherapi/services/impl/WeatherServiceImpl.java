package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.FmiApi;
import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.api.YrApi;
import com.example.weatherapi.cache.CacheDB;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.ApiStatus;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.exceptions.WeatherNotFilledException;
import com.example.weatherapi.repositories.ApiStatusRepository;
import com.example.weatherapi.services.CityService;
import com.example.weatherapi.services.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
    private final CacheManager cacheManager;
    private final String cacheName;
    private final ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<>();
    private final ApiStatusRepository apiStatusRepository;
    private static final String TEMPERATURE = "temperature";
    private static final String WIND_SPEED = "windSpeed";
    private static final String PRECIPITATION = "precipitation";
    private static final String HUMIDITY = "humidity";

    private final AtomicReference<Timestamp> lastApiStatusUpdate;

    @Autowired
    public WeatherServiceImpl(CityService cityService,
                              SmhiApi smhiApi,
                              YrApi yrApi,
                              FmiApi fmiApi,
                              CacheDB cacheDB,
                              CacheManager cacheManager,
                              ApiStatusRepository apiStatusRepository
    ) {
        this.cityService = cityService;
        this.fmiApi = fmiApi;
        this.smhiApi = smhiApi;
        this.yrApi = yrApi;
        this.cacheDB = cacheDB;
        this.cacheManager = cacheManager;
        this.apiStatusRepository = apiStatusRepository;
        this.log = LoggerFactory.getLogger(WeatherServiceImpl.class);
        this.cacheName = "cache";
        this.lastApiStatusUpdate = new AtomicReference<>(new Timestamp(0));
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
        String key = cityName.toLowerCase() + "merged";

        if (isApiStatusChanged()) {
            log.info("API statuses have changed, invalidating in-memory cache for {}", cityName);
            Objects.requireNonNull(cacheManager.getCache(cacheName)).evict(key);
        }

        Weather weatherFromCache = Objects.requireNonNull(cacheManager.getCache(cacheName)).get(key, Weather.class);
        if (weatherFromCache != null) {
            log.info("Cache hit for City: {} in the cache, returning cached data", cityName);
            return weatherFromCache;
        }

        Lock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();

        try {
            weatherFromCache = Objects.requireNonNull(cacheManager.getCache(cacheName)).get(key, Weather.class);
            if (weatherFromCache != null) {
                log.info("Cache hit for City: {} in the cache, returning cached data", cityName);
                return weatherFromCache;
            }

            City city = toModel(cityService.getCityByName(cityName));

            Optional<Weather> optionalWeather = checkForMergedWeatherData(city, key);
            if (optionalWeather.isPresent()) {
                return optionalWeather.get();
            }

            Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData = new TreeMap<>();
            Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap = new ConcurrentHashMap<>();
            List<String> successfulApis = Collections.synchronizedList(new ArrayList<>());

            List<String> enabledApis = new ArrayList<>();
            if (apiStatusRepository.findByApiName("SMHI").isActive()) enabledApis.add("SMHI");
            if (apiStatusRepository.findByApiName("YR").isActive()) enabledApis.add("YR");
            if (apiStatusRepository.findByApiName("FMI").isActive()) enabledApis.add("FMI");

            Weather mergedWeather = fetchWeatherData(city, enabledApis, mergedWeatherData, updateCountMap, successfulApis);

            mergedWeather.getWeatherData().entrySet().removeIf(entry -> entry.getValue().getWeatherCode() == -1);
            if (enabledApis.size() != 1) {
                saveDB(mergedWeather, successfulApis);
            }
            getSunriseSunset(mergedWeather);
            Objects.requireNonNull(cacheManager.getCache(cacheName)).put(key, mergedWeather);
            return mergedWeather;
        } finally {
            lock.unlock();
            locks.remove(key, lock);
        }
    }

    public Weather getWeatherMergedCustomApis(String cityName, List<String> enabledApis) {

        enabledApis = enabledApis.stream().map(String::toUpperCase).toList();

        List<String> allActiveApis = validateApis(enabledApis, apiStatusRepository);

        // If all enabled APIs are used, return the merged weather data instead of fetching it again
        if (new HashSet<>(allActiveApis).equals(new HashSet<>(enabledApis))){
            return getWeatherMerged(cityName);
        }

        City city = toModel(cityService.getCityByName(cityName));
        Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData = new TreeMap<>();
        Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap = new ConcurrentHashMap<>();
        List<String> successfulApis = Collections.synchronizedList(new ArrayList<>());

        Weather mergedWeather = fetchWeatherData(city, enabledApis, mergedWeatherData, updateCountMap, successfulApis);

        mergedWeather.getWeatherData().entrySet().removeIf(entry -> entry.getValue().getWeatherCode() == -1);

        getSunriseSunset(mergedWeather);

        return mergedWeather;
    }

    private Weather fetchWeatherData(City city, List<String> enabledApis, Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData,
                                     Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap, List<String> successfulApis) throws WeatherNotFilledException {

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        if (enabledApis.contains("SMHI")) {
            CompletableFuture<Void> smhi = fetchAndProcessWeatherData(
                    "SMHI",
                    smhiApi.fetchWeatherSmhiAsync(city),
                    mergedWeatherData,
                    updateCountMap,
                    successfulApis);
            futures.add(smhi);
        }

        if (enabledApis.contains("YR")) {
            CompletableFuture<Void> yr = fetchAndProcessWeatherData(
                    "YR",
                    yrApi.fetchWeatherYrAsync(city),
                    mergedWeatherData,
                    updateCountMap,
                    successfulApis);
            futures.add(yr);
        }

        if (enabledApis.contains("FMI")) {
            CompletableFuture<Weather> fmiWeatherFuture = fmiApi.fetchWeatherFmiAsync(city);
            CompletableFuture<Void> fmi = CompletableFuture.anyOf(
                    futures.toArray(new CompletableFuture<?>[0])
            ).thenCompose(v -> fetchAndProcessWeatherData(
                    "FMI",
                    fmiWeatherFuture,
                    mergedWeatherData,
                    updateCountMap,
                    successfulApis));
            futures.add(fmi);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        if (mergedWeatherData.isEmpty()) {
            Map<String, Boolean> apiStatus = getApiStatus();
            throw new WeatherNotFilledException("Could not connect to any weather API. API Status: " + apiStatus);
        }

        calculateAverages(mergedWeatherData, updateCountMap);
        setScaleWeatherData(mergedWeatherData);

        return Weather.builder()
                .message(createMessage(city, successfulApis))
                .weatherData(mergedWeatherData)
                .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
                .city(city)
                .build();
    }

    private synchronized boolean isApiStatusChanged() {
        Timestamp currentApiStatusUpdate = apiStatusRepository.getLastUpdateTime();
        if (currentApiStatusUpdate != null && currentApiStatusUpdate.after(lastApiStatusUpdate.get())) {
            lastApiStatusUpdate.set(currentApiStatusUpdate);
            return true;
        }
        return false;
    }

    private Optional<Weather> checkForMergedWeatherData(City city, String cacheKey) {
        Map<String, Boolean> apiStatusMap = getApiStatus();

        Weather weather = cacheDB.getWeatherFromCache(
                city.getName(),
                apiStatusMap.getOrDefault("SMHI", false),
                apiStatusMap.getOrDefault("YR", false),
                apiStatusMap.getOrDefault("FMI", false)
        );

        if (weather != null) {
            getSunriseSunset(weather);
            Objects.requireNonNull(cacheManager.getCache(cacheName)).put(cacheKey, weather);
            return Optional.of(weather);
        }

        return Optional.empty();
    }

    public void saveDB(Weather weather, List<String> successfulApis) {
        cacheDB.save(
                weather,
                successfulApis.contains("SMHI"),
                successfulApis.contains("YR"),
                successfulApis.contains("FMI")
        );
    }

    private CompletableFuture<Void> fetchAndProcessWeatherData(
            String apiName,
            CompletableFuture<Weather> weatherFuture,
            Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData,
            Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap,
            List<String> successfulApis) {

        return CompletableFuture.runAsync(() -> {
            ApiStatus apiStatus = apiStatusRepository.findByApiName(apiName);
            if (apiStatus == null || !apiStatus.isActive()) return;
            if (apiName.equals("FMI")
                    && !apiStatusRepository.findByApiName("SMHI").isActive()
                    && !apiStatusRepository.findByApiName("YR").isActive()) {
                    return;
                }

            weatherFuture
                    .exceptionally(e -> {
                        log.error("Failed to fetch weather data from {}", apiName, e);
                        return null;
                    })
                    .thenAccept(weather -> {
                        if (weather != null) {
                            mergeWeatherDataIntoMergedData(weather.getWeatherData(), apiName, mergedWeatherData, updateCountMap);
                            successfulApis.add(apiName);
                        }
                    }).join();
        });
    }

    private synchronized void mergeWeatherDataIntoMergedData(
            Map<ZonedDateTime, Weather.WeatherData> newData,
            String api,
            Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData,
            Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap) {

        AtomicInteger newDataCount = new AtomicInteger();
        AtomicInteger mergedDataCount = new AtomicInteger();

        newData.forEach((key, newDataItem) -> {
            if (mergedWeatherData.containsKey(key)) {
                updateDataField(newDataItem.getTemperature(),
                        mergedWeatherData.get(key)::setTemperature,
                        mergedWeatherData.get(key).getTemperature(),
                        key,
                        TEMPERATURE,
                        updateCountMap);
                updateDataField(newDataItem.getPrecipitation(),
                        mergedWeatherData.get(key)::setPrecipitation,
                        mergedWeatherData.get(key).getPrecipitation(),
                        key,
                        PRECIPITATION,
                        updateCountMap);
                updateDataField(newDataItem.getWindSpeed(),
                        mergedWeatherData.get(key)::setWindSpeed,
                        mergedWeatherData.get(key).getWindSpeed(),
                        key,
                        WIND_SPEED,
                        updateCountMap);
                updateDataField(newDataItem.getHumidity(),
                        mergedWeatherData.get(key)::setHumidity,
                        mergedWeatherData.get(key).getHumidity(),
                        key,
                        HUMIDITY,
                        updateCountMap);

                if (newDataItem.getWindDirection() != -99f) {
                    mergedWeatherData.get(key).setWindDirection(
                            getAvgWindDirection(mergedWeatherData.get(key).getWindDirection(), newDataItem.getWindDirection()));
                }
                mergedWeatherData.get(key).setWeatherCode(determineWeatherCode(api, mergedWeatherData.get(key), newDataItem));
                mergedDataCount.incrementAndGet();
            } else if (!api.equals("FMI")) {
                mergedWeatherData.put(key, newDataItem);
                updateCount(key, TEMPERATURE, updateCountMap);
                updateCount(key, WIND_SPEED, updateCountMap);
                updateCount(key, PRECIPITATION, updateCountMap);
                updateCount(key, HUMIDITY, updateCountMap);
                newDataCount.incrementAndGet();
            }
        });
        log.info("{} weather data from {}", (newDataCount.get() > 0 && mergedDataCount.get() == 0) ? "Added" : "Merged",  api);
    }

    private void updateDataField(float newDataValue, Consumer<Float> setter, float existingValue, ZonedDateTime key,
                                 String fieldName, Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap) {
        if (newDataValue != -99f) {
            setter.accept(existingValue + newDataValue);
            updateCount(key, fieldName, updateCountMap);
        }
    }

    private int determineWeatherCode(String api, Weather.WeatherData existingData, Weather.WeatherData newDataItem) {
        if (api.equals("SMHI")) {
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

    private Map<String, Boolean> getApiStatus() {
        Map<String, Boolean> apiStatusMap = new HashMap<>();
        List<ApiStatus> apiStatusList = apiStatusRepository.findAll();
        for (ApiStatus apiStatus : apiStatusList) {
            apiStatusMap.put(apiStatus.getApiName(), apiStatus.isActive());
        }
        return apiStatusMap;
    }

}