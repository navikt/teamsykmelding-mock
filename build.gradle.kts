import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "no.nav.syfo"
version = "1.0.0"


val coroutinesVersion = "1.10.2"
val jacksonVersion = "3.2.2"
val kluentVersion = "1.73"
val ktorVersion = "3.5.2"
val logbackVersion = "1.6.3"
val logstashEncoderVersion = "9.0"
val prometheusVersion = "0.16.0"
val mockkVersion = "1.14.4"
val testcontainerVersion = "2.0.3"
val nimbusVersion = "9.40"
val kotlinVersion = "2.4.10"
val jaxbRuntimeVersion = "2.4.0-b180830.0438"
val jaxbApiVersion = "2.4.0-b180830.0359"
val sysfoXmlCodeGen = "2.0.1"
val javaTimeAdapterVersion = "1.1.3"
val ktfmtVersion = "0.56"
val junitJupiterVersion = "6.1.3"
val koinVersion = "4.1.0-Beta8"
val diagnosekoderVersion = "1.2026.0"
val ibmMqVersion = "10.0.0.0"
val kafkaVersion = "4.3.1"


plugins {
    id("application")
    id("com.diffplug.spotless") version "8.10.1"
    kotlin("jvm") version "2.4.10"
}

application {
    mainClass.set("no.nav.syfo.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}


dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$coroutinesVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache5:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson3:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")
    implementation("org.apache.kafka:kafka-streams:$kafkaVersion")

    implementation("com.ibm.mq:com.ibm.mq.jakarta.client:$ibmMqVersion")

    implementation("no.nav.helse:diagnosekoder:$diagnosekoderVersion")

    implementation("no.nav.helse.xml:xmlfellesformat:$sysfoXmlCodeGen")
    implementation("no.nav.helse.xml:kith-hodemelding:$sysfoXmlCodeGen")
    implementation("no.nav.helse.xml:sm2013:$sysfoXmlCodeGen")
    implementation("no.nav.helse.xml:legeerklaering:$sysfoXmlCodeGen")
    implementation("no.nav.helse.xml:papirsykemelding:$sysfoXmlCodeGen")
    implementation("javax.xml.bind:jaxb-api:$jaxbApiVersion")
    implementation("org.glassfish.jaxb:jaxb-runtime:$jaxbRuntimeVersion")
    implementation("com.migesok:jaxb-java-time-adapters:$javaTimeAdapterVersion")

    implementation("tools.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    implementation("tools.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("tools.jackson.dataformat:jackson-dataformat-xml:${jacksonVersion}")
    implementation("tools.jackson.module:jackson-module-jaxb-annotations:${jacksonVersion}")

    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.testcontainers:testcontainers-kafka:$testcontainerVersion")

    implementation("io.insert-koin:koin-ktor3:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


kotlin {
    jvmToolchain(25)
}

tasks {
    
    test {
        useJUnitPlatform {}
        testLogging {
            events("skipped", "failed")
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }


    spotless {
        kotlin { ktfmt(ktfmtVersion).kotlinlangStyle() }
        check {
            dependsOn("spotlessApply")
        }
    }
}
