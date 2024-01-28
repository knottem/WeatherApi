    package com.example.weatherapi.util;

    import com.example.weatherapi.domain.entities.CityEntity;
    import com.example.weatherapi.domain.entities.WeatherEntity;
    import com.example.weatherapi.domain.weather.Weather;
    import com.example.weatherapi.exceptions.CityNotFoundException;
    import com.example.weatherapi.repositories.CityRepository;
    import com.example.weatherapi.repositories.WeatherDataRepository;
    import com.example.weatherapi.repositories.WeatherEntityRepository;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.Optional;

    import static com.example.weatherapi.util.WeatherMapper.*;

    @Service
    public class Cache {
        private final Logger logger;
        private final WeatherEntityRepository weatherEntityRepository;

        @Value("${cache.time.in.minutes}")
        private int cacheTimeInMinutes;
        private final CityRepository cityRepository;

        @Autowired
        private Cache(WeatherEntityRepository weatherEntityRepository,
                      WeatherDataRepository weatherDataRepository,
                      CityRepository cityRepository) {
            logger = LoggerFactory.getLogger(Cache.class);
            this.weatherEntityRepository = weatherEntityRepository;
            this.cityRepository = cityRepository;
        }

        public Cache() {
            // Default, no-argument constructor
            logger = LoggerFactory.getLogger(Cache.class);
            this.weatherEntityRepository = null;
            this.cityRepository = null;
        }

        public Weather getWeatherFromCache(String cityName) {
            if (cacheTimeInMinutes < 0) {
                logger.warn("Cache time in minutes is negative, setting it to default value of 60 minutes");
                cacheTimeInMinutes = 60;
            }

            Optional<WeatherEntity> cachedWeatherOptional = weatherEntityRepository.findLatestByCityName(cityName);

            if (cachedWeatherOptional.isPresent()) {
                WeatherEntity cachedWeather = cachedWeatherOptional.get();
                if (cachedWeather.isValid(cacheTimeInMinutes)) {
                    logger.info("Cache hit for City: {} in the database, returning cached data", cityName);
                    return convertToWeather(cachedWeather);
                } else {
                    logger.info("Cache expired for City: {} in the database, fetching new data", cityName);
                    //Maybe remove cache, but for now we keep every cache in the database. If so just delete the cachedWeather object
                    return null;
                }
            } else {
                logger.info("Cache doesn't exist for City: {} in the database, fetching new data", cityName);
            }
            return null;
        }

        @Transactional
        public void save(Weather weather) {
            CityEntity cityEntity = cityRepository.findByNameIgnoreCase(weather.getCity().getName())
                    .orElseThrow(() -> new CityNotFoundException("City not found with ID: " + weather.getCity().getName()));
            WeatherEntity weatherEntity = weatherEntityRepository.save(convertToWeatherEntity(weather, cityEntity));
            logger.info("Saved weather data to cache with id: {} and key: {}", weatherEntity.getId(), weather.getCity().getName());
        }
    }
