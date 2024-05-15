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

CREATE TABLE latest_weather_api (
    id VARCHAR(36) NOT NULL,
    latest_weather_id VARCHAR(36) NOT NULL,
    city_id VARCHAR(36) NOT NULL,
    smhi BOOLEAN NOT NULL DEFAULT FALSE,
    yr BOOLEAN NOT NULL DEFAULT FALSE,
    fmi BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    FOREIGN KEY (latest_weather_id) REFERENCES weather (id),
    FOREIGN KEY (city_id) REFERENCES city (id)
);