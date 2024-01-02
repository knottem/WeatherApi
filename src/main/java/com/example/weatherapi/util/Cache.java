    package com.example.weatherapi.util;

    import com.example.weatherapi.domain.entities.CityEntity;
    import com.example.weatherapi.domain.entities.WeatherCacheEntity;
    import com.example.weatherapi.domain.entities.WeatherEntity;
    import com.example.weatherapi.domain.weather.Weather;
    import com.example.weatherapi.exceptions.CityNotFoundException;
    import com.example.weatherapi.repositories.CityRepository;
    import com.example.weatherapi.repositories.WeatherCacheRepository;
    import com.example.weatherapi.repositories.WeatherDataRepository;
    import com.example.weatherapi.repositories.WeatherEntityRepository;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.util.Optional;

    import static com.example.weatherapi.util.WeatherMapper.*;

    @Service
    public class Cache {

        private final Logger logger;
        private final WeatherCacheRepository weatherCacheRepository;
        private final WeatherEntityRepository weatherEntityRepository;
        private final WeatherDataRepository weatherDataRepository;

        @Value("${cache.time.in.minutes}")
        private int cacheTimeInMinutes;
        private final CityRepository cityRepository;

        @Autowired
        private Cache(WeatherCacheRepository weatherCacheRepository,
                      WeatherEntityRepository weatherEntityRepository,
                      WeatherDataRepository weatherDataRepository,
                      CityRepository cityRepository) {
            logger = LoggerFactory.getLogger(Cache.class);
            this.weatherCacheRepository = weatherCacheRepository;
            this.weatherEntityRepository = weatherEntityRepository;
            this.weatherDataRepository = weatherDataRepository;
            this.cityRepository = cityRepository;
        }

        public Cache() {
            // Default, no-argument constructor
            logger = LoggerFactory.getLogger(Cache.class);
            this.weatherCacheRepository = null;
            this.weatherEntityRepository = null;
            this.weatherDataRepository = null;
            this.cityRepository = null;
        }

        public Weather getWeatherFromCache(String key) {
            if (cacheTimeInMinutes < 0) {
                logger.warn("Cache time in hours is negative, setting it to default value of 60 minutes");
                cacheTimeInMinutes = 60;
            }

            Optional<WeatherCacheEntity> cachedWeatherOptional = weatherCacheRepository.findLatestByCacheKey(key);

            if (cachedWeatherOptional.isPresent()) {
                WeatherCacheEntity cachedWeather = cachedWeatherOptional.get();
                if (cachedWeather.isValid(cacheTimeInMinutes)) {
                    logger.info("Cache hit for key: {} in the database, returning cached data", key);
                    return convertToWeather(cachedWeather.getWeather());
                } else {
                    logger.info("Cache expired for key: {} in the database, fetching new data", key);
                    //Maybe remove cache, but for now we keep every cache in the database. If so just delete the cachedWeather object
                    return null;
                }
            } else {
                logger.info("Cache doesn't exist for key: {} in the database, fetching new data", key);
            }
            return null;
        }

        @Transactional
        public void save(String key, Weather weather) {
            CityEntity cityEntity = cityRepository.findByNameIgnoreCase(weather.getCity().getName())
                    .orElseThrow(() -> new CityNotFoundException("City not found with ID: " + weather.getCity().getName()));
            WeatherEntity weatherEntity = weatherEntityRepository.save(convertToWeatherEntity(weather, cityEntity));
            weatherDataRepository.saveAll(convertToWeatherDataEntity(weather.getWeatherData(), weatherEntity));
            WeatherCacheEntity weatherCacheEntity =
            weatherCacheRepository.save(WeatherCacheEntity.builder()
                    .cacheKey(key)
                    .timestamp(LocalDateTime.now())
                    .weather(weatherEntity)
                    .build());

            logger.info("Saved weather data to cache with id: {} and key: {}", weatherCacheEntity.getId(), key);
        }

        public void clear() {
            weatherCacheRepository.deleteAll();
        }


    }
