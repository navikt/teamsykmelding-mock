# teamsykmelding-mock-backend
This project contains the application code and infrastructure for teamsykmelding-mock-backend

## Technologies used
* Kotlin
* Ktor
* Gradle
* MQ
* Junit
* Jdk 21

### Prerequisites
Make sure you have the Java JDK 21 installed
You can check which version you have installed using this command:
``` bash
java -version
```

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

### Upgrading the gradle wrapper
Find the newest version of gradle here: https://gradle.org/releases/ Then run this command:

```./gradlew wrapper --gradle-version $gradleVersjon```


### Swagger api doc
The Swagger api doc is available here
https://teamsykmelding-mock-backend.intern.dev.nav.no/swagger

## Contact

This project is maintained by [navikt/teamsykmelding](CODEOWNERS)

Questions and/or feature requests? Please create an [issue](https://github.com/navikt/teamsykmelding-mock-backend/issues)

If you work in [@navikt](https://github.com/navikt) you can reach us at the Slack
channel [#team-sykmelding](https://nav-it.slack.com/archives/CMA3XV997)
