# WeatherAPI

## Description
This is a weather restapi that returns the weather of a city. For now it only returns the temperature for the next few days. The api is built using spring boot and fetches from smhi's open api.

This project is just a side hobby project that I use to learn more about spring boot and rest APIs.

## How to run
This program runs in docker and docker-compose. To run it you need to have both installed. Then you can run the following commands:
First you'll need to create a .env file in the root directory with the following variables:
```bash
DB_HOST= # The host of your database
DB_PORT= # The port of your database
DB_SCHEMA= # The schema of your database
DB_USER= # The user of your database
DB_PASSWORD= # The password of your database
CACHE_TIME_IN_HOURS= # The time in hours that the cache should be valid
DOMAIN= # The domain of the api, used for yr api, can be ip address
GITHUB= # The github link to this project, used for yr api
USER_NAME= # The username for the api, used for spring security
USER_PASSWORD= # The password for the api, used for spring security
```
Here's an example of a .env file:
```bash
DB_HOST=citydb
DB_PORT=3306
DB_SCHEMA=weatherdb
DB_USER=cityuser
DB_PASSWORD=password
CACHE_TIME_IN_HOURS=3
DOMAIN=https://example.com
GITHUB=https://github.com/yourusername
USER_NAME=test
USER_PASSWORD=test
```
Then you can run the following commands, when you're in the root directory of weatherapi incase the dockerfile isn't working correctly:
```bash
docker build -t weatherapi .
``` 

Then you just run the docker-compose file, be sure to be in the same directory as the docker-compose file:
```bash
docker-compose up
```


## DONE
- [X] Fetch from multiple weather APIs
- [X] Add spring security
- [X] Add logging
- [X] Add basic error handling
- [X] Compare the results from the different APIs and return a merged result

## TODO
- [ ] Add tests.
- [ ] Change my basic caching to use spring boot caching
- [ ] Add more weather APIs
- [ ] Make use of more from each API instead of just the temperature
- [ ] Add more endpoints

## Made by
- [Erik Wallenius](https://github.com/knottem)
