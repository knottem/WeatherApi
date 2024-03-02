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
  - [DONE](#done)
  - [TODO](#todo)
  - [Made by](#made-by)

## Description
This is a weather restapi that returns the weather of a city. For now, it only returns the temperature for the next few days. The api is built using spring boot and fetches from smhi's open api.

This project is just a side hobby project that I use to learn more about spring boot and rest APIs.

## How to run

### Requirements
- Docker
- Docker-compose

### Running the program

You'll need an docker-compose file and a .env file to run the program. The docker-compose file is used to run the program in docker and the .env file is used to set the environment variables that the program needs to run.

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

#### .env file
The .env file is used to set the environment variables that the program needs to run. The file needs to be in the same directory as the docker-compose file. You can set the environment variables in the docker-compose file instead of using a .env file, but I prefer to use a .env file since it's easier to change the values in the file than in the docker-compose file, but for security reasons you shouldn't have sensitive information in the docker-compose file unless you're sure you won't be sharing it with anyone.

The .env file needs to have the following variables:
```
DB_SCHEMA= # The schema of your database
DB_USER= # The user of your database
DB_PASSWORD= # The password of your database
CACHE_TIME_IN_HOURS= # The time in hours that the cache should be valid
DOMAIN= # The domain of the api, used for yr api, can be ip address as well, 
        # just needs to be a valid url that the program will be running on
GITHUB= # Your github username, email or whatever you want to use to identify yourself, 
        # needed for the yr api
```
Here's an example of a .env file:
```
DB_SCHEMA=weatherdb
DB_USER=cityuser
DB_PASSWORD=password
CACHE_TIME_IN_HOURS=3
DOMAIN=https://example.com
GITHUB=https://github.com/yourusername
```

Then you just run the docker-compose file, be sure to be in the same directory as the docker-compose file
- the -d flag is optional, it just makes it run in the background and not lock your terminal

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

If you get an error when running docker-compose that complains about ports already being used, you can change the following part in the docker-compose file:
```yaml
ports:
  - "8080:8080"
```
to something like this:
```yaml
ports:
  - "8081:8080"
```
This will make the program run on port 8081 instead of 8080, change it to whatever port you want but make sure to keep the port after the colon as 8080 since that's the port the program is running on inside the container.


## DONE

- [X] Fetch from multiple weather APIs
- [X] Add spring security
- [X] Add logging
- [X] Add basic error handling
- [X] Compare the results from the different APIs and return a merged result
- [X] Use custom properties with h2 for tests
- [X] Add GitHub actions for testing.
- [X] Change my basic caching to use spring boot caching (https://spring.io/guides/gs/caching/)

## TODO

- [ ] Add 0auth2 for city management using Firebase(https://firebase.google.com/docs/auth)
- [ ] Add more tests.
- [ ] Add more weather APIs(Danish - DMI, Finnish - FMI)
- [ ] Add more endpoints

## Made by

- [Erik Wallenius](https://github.com/knottem)