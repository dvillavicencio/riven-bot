# Riven Bot for Discord
> Source code for Riven Bot

This repository has all the source code that makes the Riven Bot work. The idea is to have a Discord bot that contains very useful utilities for D2 players that use Discord as their main social media platform like retrieving weekly-rotation information for in-game activities and player-specific raid/dungeon statistics. 

## Why? 
Because I can and I want to learn through projects that I find fun and challenging.

## Stack
  - [Spring Boot](https://spring.io/projects/spring-boot) - Official documentation for Spring Boot
  - [Spring Webflux](https://spring.io/reactive) - Official documentation for Spring Reactive framework, for Project Reactor see [here](https://projectreactor.io/)
  - [Redis](https://redis.io/) - Official documentation for Redis
  - [Docker](https://www.docker.com/) - Official documentation for Docker & Docker Compose
  - [MongoDB](https://mongodb.com) - Official documentation for MongoDB
## Testing
  - [JUnit5](https://junit.org/junit5/) - Official documentation for JUnit5 for testing JVM based languages
  - [Mockito](https://site.mockito.org/) - Official documentation for Mockito
  - [AssertJ](https://assertj.github.io/doc/) - Official documentation for AssertJ assertions framework for testing
  - [Test Containers](https://java.testcontainers.org/) - Official documentation for Test Containers using Java
  - [Wiremock](https://wiremock.org/docs/) - Official documentation for Wiremock
## Observability
  - [Loki](https://grafana.com/oss/loki/) - Official documentation for OSS Loki
  - [Grafana](https://grafana.com/oss/grafana) - Official documentation for OSS Grafana
  - [Loki4j](https://github.com/loki4j/loki-logback-appender) - Official GitHub repository for Loki4j Logback Appender

Any information regarding versions of specific dependencies you can find it in the `build.gradle` file at the root directory of the project

_Bot invite link_: (To be added in the future, current version 0.1.0-alpha isn't stable enough for use)


## Raid Stats
So far the bot has limited functionality and the focus of the next releases will be to polish out the `/raid_stats` command, improving the time it takes to load hundreds, even thousands of raid reports to calculate the numbers for an individual player, as well as adding filters to the command itself if the user would prefer to narrow down their search to only one raid at a time. 

Here's a small demo of how the Raid Statistics are utilized and presented when using RivenBot:

https://github.com/user-attachments/assets/d296c71e-2061-47ba-af2a-b2c3bd4b2f9e


## Contributing
This is meant to be a personal-project, therefore any issues or suggestions are appreciated but the development of the bot is under my responsibility and of selected trusted individuals. If you have an awesome idea for this bot, you can open a 
GitHub issue; that way we can discuss the idea further.
