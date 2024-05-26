package com.example.weatherapi.cache;

import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.domain.entities.LatestWeatherApiEntity;
import com.example.weatherapi.domain.entities.WeatherEntity;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.exceptions.CityNotFoundException;
import com.example.weatherapi.repositories.CityRepository;
import com.example.weatherapi.repositories.LatestWeatherApiRepository;
import com.example.weatherapi.repositories.WeatherEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.weatherapi.util.WeatherMapper.*;
import static com.example.weatherapi.util.WeatherValidation.isWeatherValid;

@Service
public class CacheDB {

    private final Logger logger;
    private final WeatherEntityRepository weatherEntityRepository;
    private final LatestWeatherApiRepository latestWeatherApiRepository;
    private final CityRepository cityRepository;

    @Value("${cache.time.in.minutes}")
    private int cacheTimeInMinutes;

    @Autowired
    private CacheDB(WeatherEntityRepository weatherEntityRepository,
                    LatestWeatherApiRepository latestWeatherApiRepository,
                    CityRepository cityRepository) {
        logger = LoggerFactory.getLogger(CacheDB.class);
        this.weatherEntityRepository = weatherEntityRepository;
        this.cityRepository = cityRepository;
        this.latestWeatherApiRepository = latestWeatherApiRepository;

    }

    public CacheDB() {
        // Default, no-argument constructor
        logger = LoggerFactory.getLogger(CacheDB.class);
        this.weatherEntityRepository = null;
        this.cityRepository = null;
        this.latestWeatherApiRepository = null;
    }

    @Transactional
    public Weather getWeatherFromCache(String cityName, boolean smhi, boolean yr, boolean fmi) {
        if (cacheTimeInMinutes < 0) {
            logger.warn("Cache time in minutes is negative, setting it to default value of 60 minutes");
            cacheTimeInMinutes = 60;
        }

        CityEntity cityEntity = cityRepository.findByNameIgnoreCase(cityName)
                .orElseThrow(() -> new RuntimeException("City not found: " + cityName));

        Optional<LatestWeatherApiEntity> latestApiOptional = latestWeatherApiRepository
                .findByCityAndSmhiAndYrAndFmi(cityEntity, smhi, yr, fmi);

        String apisUsed = formatApisUsed(smhi, yr, fmi);

        if (latestApiOptional.isPresent()) {
            WeatherEntity cachedWeather = latestApiOptional.get().getLatestWeather();
            if (cachedWeather != null && isWeatherValid(cachedWeather.getTimeStamp(), cacheTimeInMinutes)) {
                logger.info("Database cache hit for city: {} with APIs: {} in the database. Returning cached data.", cityName, apisUsed);
                return convertToWeather(cachedWeather);
            } else {
                logger.debug("Database cache expired for city: {} with APIs: {} in the database", cityName, apisUsed);
                return null;
            }
        } else {
            logger.debug("Database cache doesn't exist for city: {} with APIs: {} in the database", cityName, apisUsed);
        }
        return null;
    }

    @Transactional
    public void save(Weather weather, boolean smhi, boolean yr, boolean fmi) {
        CityEntity cityEntity = cityRepository.findByNameIgnoreCase(weather.getCity().getName())
                .orElseThrow(() -> new CityNotFoundException("City not found: " + weather.getCity().getName()));

        WeatherEntity weatherEntity = weatherEntityRepository.save(convertToWeatherEntity(weather, cityEntity));

        LatestWeatherApiEntity latestWeatherApi = latestWeatherApiRepository
                .findByCityAndSmhiAndYrAndFmi(cityEntity, smhi, yr, fmi)
                .orElse(null);

        if (latestWeatherApi == null) {
            latestWeatherApi = new LatestWeatherApiEntity();
            latestWeatherApi.setCity(cityEntity);
            latestWeatherApi.setSmhi(smhi);
            latestWeatherApi.setYr(yr);
            latestWeatherApi.setFmi(fmi);
        }

        latestWeatherApi.setLatestWeather(weatherEntity);
        latestWeatherApiRepository.save(latestWeatherApi);

        String apisUsed = formatApisUsed(smhi, yr, fmi);
        logger.info("Saved weather data to cache with city: {}, and APIs: {} ", cityEntity.getName(), apisUsed);
    }

    private String formatApisUsed(boolean smhi, boolean yr, boolean fmi) {
        List<String> apis = new ArrayList<>();
        if (smhi) apis.add("SMHI");
        if (yr) apis.add("YR");
        if (fmi) apis.add("FMI");
        return String.join(", ", apis);
    }
}
