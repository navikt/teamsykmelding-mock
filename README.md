# teamsykmelding-mock-backend
This project contains the application code and infrastructure for teamsykmelding-mock-backend
## Technologies used
* Kotlin
* Ktor
* Gradle

## Getting started
### Getting github-package-registry packages NAV-IT
Some packages used in this repo is uploaded to the Github Package Registry which requires authentication. It can, for example, be solved like this in Gradle:
```
val githubUser: String by project
val githubPassword: String by project
repositories {
    maven {
        credentials {
            username = githubUser
            password = githubPassword
        }
        setUrl("https://maven.pkg.github.com/navikt/syfosm-common")
    }
}
```

`githubUser` and `githubPassword` can be put into a separate file `~/.gradle/gradle.properties` with the following content:

```                                                     
githubUser=x-access-token
githubPassword=[token]
```

Replace `[token]` with a personal access token with scope `read:packages`.

Alternatively, the variables can be configured via environment variables:

* `ORG_GRADLE_PROJECT_githubUser`
* `ORG_GRADLE_PROJECT_githubPassword`

or the command line:

```
./gradlew -PgithubUser=x-access-token -PgithubPassword=[token]
```

### Building the application
#### Compile and package application
To build locally and run the integration tests you can simply run `./gradlew shadowJar` or  on windows 
`gradlew.bat shadowJar`

#### Creating a docker image
Creating a docker image should be as simple as `docker build -t teamsykmelding-mock-backend .`

#### Running a docker image
`docker run --rm -it -p 8080:8080 teamsykmelding-mock-backend`

## Oppgradering av gradle wrapper
Finn nyeste versjon av gradle her: https://gradle.org/releases/

```./gradlew wrapper --gradle-version $gradleVersjon```


### Kontakt

Dette prosjektet er vedlikeholdt av navikt/teamsykmelding

Spørsmål og/eller forbedrings ønsker? Gjerne lag ein [issue](https://github.com/navikt/teamsykmelding-mock-backend/issues).

Dersom du jobber i NAV [@navikt](https://github.com/navikt) kan du nå oss på slack 
kanalen [#team-sykmelding](https://nav-it.slack.com/archives/CMA3XV997).
