# WeatherAPI

## Description
This is a weather restapi that returns the weather of a city. For now, it only returns the temperature for the next few days. The api is built using spring boot and fetches from smhi's open api.

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
DOMAIN= # The domain of the api, used for yr api, can be ip address as well, 
        # just needs to be a valid url that the program will be running on
GITHUB= # Your github username, email or whatever you want to use to identify yourself, 
        # needed for the yr api
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


## DONE
- [X] Fetch from multiple weather APIs
- [X] Add spring security
- [X] Add logging
- [X] Add basic error handling
- [X] Compare the results from the different APIs and return a merged result
- [X] Use custom properties with h2 for tests
- [X] Add GitHub actions for testing.

## TODO


- [ ] Add documentation to every class and method
- [ ] Add more tests.
- [ ] Change my basic caching to use spring boot caching (https://spring.io/guides/gs/caching/)
- [ ] Add more weather APIs(Danish - DMI, Finnish - FMI)
- [ ] Add more endpoints
- [ ] Clean up the code, make it more readable and remove unused code.

## Made by
- [Erik Wallenius](https://github.com/knottem)
