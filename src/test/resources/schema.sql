-- Create city table
CREATE TABLE city (
    id varchar(36) NOT NULL,
    lat double NOT NULL,
    lon double NOT NULL,
    name varchar(255) DEFAULT NULL,
    PRIMARY KEY (id)
);

-- Create auth table
CREATE TABLE auth (
    id varchar(36) NOT NULL,
    username varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    role varchar(255) NOT NULL CHECK (role IN ('ADMIN', 'USER', 'SUPERUSER')),
    PRIMARY KEY (id)
);

-- Create weather table
CREATE TABLE weather (
    id varchar(36) NOT NULL,
    time_stamp timestamp DEFAULT NULL,
    message varchar(255) DEFAULT NULL,
    city_id varchar(36) DEFAULT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (city_id) REFERENCES city (id)
);

-- Create weather_cache table
CREATE TABLE weather_cache (
    id varchar(36) NOT NULL,
    timestamp timestamp DEFAULT NULL,
    weather_id varchar(36) DEFAULT NULL,
    cache_key varchar(255) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (weather_id),
    UNIQUE (cache_key, weather_id),
    FOREIGN KEY (weather_id) REFERENCES weather (id) ON DELETE CASCADE
);

-- Create weather_data table
CREATE TABLE weather_data (
    id varchar(36) NOT NULL,
    precipitation float DEFAULT NULL,
    temperature float DEFAULT NULL,
    weather_code int DEFAULT NULL,
    wind_direction float DEFAULT NULL,
    wind_speed float DEFAULT NULL,
    valid_time timestamp DEFAULT NULL,
    weather_id varchar(36) DEFAULT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (weather_id) REFERENCES weather (id) ON DELETE CASCADE
);