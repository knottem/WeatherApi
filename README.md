# WeatherAPI

## Table of contents
- [WeatherAPI](#weatherapi)
  - [Table of contents](#table-of-contents)
  - [Description](#description)
  - [How to run](#how-to-run)
    - [Requirements](#requirements)
    - [Running the program](#running-the-program)
      - [Docker-compose](#docker-compose)
      - [.env file](#env-file)
  - [Troubleshooting](#troubleshooting)
    - [Docker-compose](#docker-compose-1)
  - [Done](#done)
  - [TODO](#todo)
  - [STATUS](#status)
  - [Made by](#made-by)

## Description
his is a weather REST API that provides weather information for cities, including temperature, precipitation, and wind data. The API intelligently merges data from multiple sources like SMHI, YR, and FMI to provide reliable and comprehensive weather forecasts.

This project is a side hobby to learn more about Spring Boot, REST APIs, and Dockerized deployments.

## How to run

### Requirements
1. **Docker**: Install Docker from [Docker's official site](https://www.docker.com/).
2. **Docker Compose**: Install Docker Compose (usually bundled with Docker Desktop).

### Running the Program
To run the WeatherAPI, you need:
1. A `docker-compose` file (defines how to build and run the services).
2. A `.env` file (holds your environment variables).

The following instructions will guide you step-by-step.

You'll need a docker-compose file and a .env file to run the program. The docker-compose file is used to run the program in docker and the .env file is used to set the environment variables that the program needs to run.

#### Docker-compose

The docker-compose file is used to run the program in docker. To run it you need to have docker-compose installed.
Then you can use this docker-compose file:
```yaml
version: "3.9"

services:
  citydb:
    image: ghcr.io/knottem/weatherapi-db:latest
    container_name: citydatabase
    networks:
      - api-network
    ports:
      - "3306:3306"
    environment:
      - MYSQL_USER=${DB_USER}
      - MYSQL_PASSWORD=${DB_PASSWORD}
      - MYSQL_DATABASE=${DB_SCHEMA}
      - MYSQL_RANDOM_ROOT_PASSWORD=yes
    healthcheck:
      test: ["CMD-SHELL", "mysql -u${DB_USER} -p${DB_PASSWORD} ${DB_SCHEMA} -e 'SELECT 1'"]
      interval: 20s
      timeout: 10s
      retries: 3

  weatherapi:
    image: ghcr.io/knottem/weatherapi:latest
    container_name: apiweather
    networks:
      - api-network
    ports:
      - "8080:8080"
    depends_on:
      citydb:
        condition: service_healthy
    environment:
      - DB_HOST=citydb
      - DB_PORT=3306
      - DB_USER=${DB_USER}
      - DB_PASSWORD=${DB_PASSWORD}
      - DB_SCHEMA=${DB_SCHEMA}
      - CACHE_TIME_IN_HOURS=${CACHE_TIME_IN_HOURS}
      - DOMAIN=${DOMAIN}
      - GITHUB=${GITHUB}
  
networks:
  api-network:
    driver: bridge
```

### .env file
The `.env` file is used to set the environment variables required to run the program. 
It should be located in the same directory as the `docker-compose` file. 
For obvious reason (security), avoid including sensitive information in the `docker-compose` file unless you are certain it won't be shared.

#### Required Variables

These variables must be configured for the application to run properly:
```
DB_SCHEMA= # The schema of your database
DB_USER= # The user of your database
DB_PASSWORD= # The password of your database
DOMAIN= # The domain of the api, used for yr api, can be ip address as well, 
        # just needs to be a valid url that the program will be running on
GITHUB= # Your github username, email or whatever you want to use to identify yourself, 
        # needed for the yr api

```

#### Optional Variables

```
CACHE_TIME_IN_MINUTES= # Cache validity time in minutes (default: 60)

# Rate limiter settings (default values are suitable for most cases):
SMHI_RL_MIN_MS=200 # Minimum interval (ms) between requests
SMHI_RL_BURST=1000 # Burst capacity
SMHI_RL_DAILY=10000 # Daily capacity

YR_RL_MIN_MS=200
YR_RL_BURST=1000
YR_RL_DAILY=10000

FMI_RL_MIN_MS=200
FMI_RL_BURST=600
FMI_RL_DAILY=10000
```


#### Example ```.env``` file

Here's an example of a .env file:
```
DB_SCHEMA=weatherdb
DB_USER=cityuser
DB_PASSWORD=password
DOMAIN=https://example.com
GITHUB=https://github.com/yourusername
CACHE_TIME_IN_MINUTES=60
SMHI_RL_MIN_MS=200
SMHI_RL_BURST=1000
SMHI_RL_DAILY=10000
YR_RL_MIN_MS=200
YR_RL_BURST=600
YR_RL_DAILY=10000
FMI_RL_MIN_MS=200
FMI_RL_BURST=1000
FMI_RL_DAILY=10000
```

#### Notes:
- **Default Values:** If you don't set a variable in `.env`, the application will use the default values provided in `application.properties`.
- **Rate Limiter Settings:** These values determine how many API requests can be made in a given timeframe. Modify them if your API provider has stricter rate limits.

then you run the following command:
```bash
docker-compose up -d
```

To stop the program and remove the containers, run the following command:
- the "--rmi all" flag is optional, it just removes the images as well, not just the containers
```bash
docker-compose down --rmi all
```

To update the program, run the following commands:
```bash
docker-compose down
docker-compose pull
docker-compose up -d
```
this will stop the program, pull the latest version from docker hub and then start it again.

## Troubleshooting

### Docker-compose

#### 1. Port Conflicts
If you encounter an error like "port already in use," modify the following in `docker-compose.yml`:
```yaml
ports:
  - "8080:8080"
  ```
Change the first port to an unused one, such as:
```yaml
ports:
- "8081:8080"
```

This will make the program run on port 8081 instead of 8080, change it to whatever port you want but make sure to keep the port after the colon as 8080 since that's the port the program is running on inside the container.

#### 2. Missing .env File

If Docker Compose complains about missing variables, ensure your .env file exists in the same directory as the docker-compose.yml. 
You can create one using the template provided in the README.

## Done

- [X] Fetch from multiple weather APIs
- [X] Add spring security
- [X] Add logging
- [X] Add basic error handling
- [X] Compare the results from the different APIs and return a merged result
- [X] Use custom properties with h2 for tests
- [X] Add GitHub actions for testing.
- [X] Change my basic caching to use Caffeine caching
- [X] Add more weather APIs(Finnish - FMI)
- [X] Add rate limiting

## TODO

- [ ] Add 0auth2 for city management using perhaps Firebase(https://firebase.google.com/docs/auth)

## Status

### Main Workflow
Builds, releases, and deploys the production environment from the main branch.<br>
![Build Status](https://github.com/knottem/weatherapi/actions/workflows/main.yml/badge.svg)

### Test Workflow
Builds, tests, releases, and deploys the development environment from the dev branch.<br>
![Build Status](https://github.com/knottem/weatherapi/actions/workflows/dev.yml/badge.svg)

## Made by

- [Erik Wallenius](https://github.com/knottem)