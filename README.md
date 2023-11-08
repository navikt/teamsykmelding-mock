# teamsykmelding-mock-backend
This project contains the application code and infrastructure for teamsykmelding-mock-backend

## Technologies used
* Kotlin
* Ktor
* Gradle
* MQ
* Junit
* Jdk 21

## Getting started
### Building the application
#### Compile and package application
To build locally and run the integration tests you can simply run 
``` bash
./gradlew shadowJar
```
or  on windows 
`gradlew.bat shadowJar`

#### Creating a docker image
Creating a docker image should be as simple as
``` bash 
docker build -t teamsykmelding-mock-backend .
```

#### Running a docker image
``` bash
docker run --rm -it -p 8080:8080 teamsykmelding-mock-backend
```

## Oppgradering av gradle wrapper
Finn nyeste versjon av gradle her: https://gradle.org/releases/

``` bash
./gradlew wrapper --gradle-version $gradleVersjon
```

## Contact

This project is maintained by [navikt/teamsykmelding](CODEOWNERS)

Questions and/or feature requests? Please create an [issue](https://github.com/navikt/teamsykmelding-mock-backend/issues)

If you work in [@navikt](https://github.com/navikt) you can reach us at the Slack
channel [#team-sykmelding](https://nav-it.slack.com/archives/CMA3XV997)
