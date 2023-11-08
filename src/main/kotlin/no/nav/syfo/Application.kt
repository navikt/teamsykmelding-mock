package no.nav.syfo

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.network.sockets.SocketTimeoutException
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.prometheus.client.hotspot.DefaultExports
import java.time.Duration
import java.util.*
import no.nav.syfo.azuread.AccessTokenClient
import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.legeerklaering.LegeerklaeringService
import no.nav.syfo.legeerklaering.api.registrerLegeerklaeringApi
import no.nav.syfo.metrics.monitorHttpRequests
import no.nav.syfo.mq.connectionFactory
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.narmesteleder.api.registrerNarmestelederApi
import no.nav.syfo.narmesteleder.kafka.NlResponseProducer
import no.nav.syfo.narmesteleder.kafka.model.NlResponseKafkaMessage
import no.nav.syfo.no.nav.syfo.routes.nais.isready.naisPrometheusRoute
import no.nav.syfo.oppgave.OppgaveClient
import no.nav.syfo.papirsykmelding.PapirsykmeldingService
import no.nav.syfo.papirsykmelding.api.registrerPapirsykmeldingApi
import no.nav.syfo.papirsykmelding.client.DokarkivClient
import no.nav.syfo.papirsykmelding.client.NorskHelsenettClient
import no.nav.syfo.papirsykmelding.client.SyfosmpapirreglerClient
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.routes.nais.isalive.naisIsAliveRoute
import no.nav.syfo.routes.nais.isready.naisIsReadyRoute
import no.nav.syfo.sykmelding.SlettSykmeldingService
import no.nav.syfo.sykmelding.SykmeldingService
import no.nav.syfo.sykmelding.api.registrerSykmeldingApi
import no.nav.syfo.sykmelding.client.SyfosmregisterClient
import no.nav.syfo.sykmelding.client.SyfosmreglerClient
import no.nav.syfo.sykmelding.kafka.SykmeldingStatusKafkaProducer
import no.nav.syfo.sykmelding.kafka.TombstoneKafkaProducer
import no.nav.syfo.utenlandsk.api.registrerUtenlandskPapirsykmeldingApi
import no.nav.syfo.utenlandsk.service.UtenlandskSykmeldingService
import no.nav.syfo.util.JacksonKafkaSerializer
import no.nav.syfo.util.JacksonNullableKafkaSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("no.nav.syfo.teamsykmelding-mock-backend")
val objectMapper: ObjectMapper =
    ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }

fun main() {

    val embeddedServer =
        embeddedServer(
            Netty,
            port = EnvironmentVariables().applicationPort,
            module = Application::module,
        )
    Runtime.getRuntime()
        .addShutdownHook(
            Thread {
                logger.info("Shutting down application from shutdown hook")
                embeddedServer.stop(
                    Duration.ofSeconds(10).toMillis(),
                    Duration.ofSeconds(10).toMillis()
                )
            },
        )
    embeddedServer.start(true)
}

fun Application.module() {
    val env = EnvironmentVariables()
    val serviceUser = ServiceUser()
    DefaultExports.initialize()
    val applicationState = ApplicationState()

    val connection =
        connectionFactory(env)
            .apply {
                sslSocketFactory = null
                sslCipherSuite = null
            }
            .createConnection(serviceUser.username, serviceUser.password)
    connection.start()

    val config: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
        install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                when (exception) {
                    is SocketTimeoutException ->
                        throw ServiceUnavailableException(exception.message)
                }
            }
        }
        install(HttpRequestRetry) {
            constantDelay(100, 0, false)
            retryOnExceptionIf(3) { request, throwable ->
                logger.warn("Caught exception ${throwable.message}, for url ${request.url}")
                true
            }
            retryIf(maxRetries) { request, response ->
                if (response.status.value.let { it in 500..599 }) {
                    logger.warn(
                        "Retrying for statuscode ${response.status.value}, for url ${request.url}"
                    )
                    true
                } else {
                    false
                }
            }
        }
        install(HttpTimeout) {
            socketTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            requestTimeoutMillis = 30_000
        }
    }
    val httpClient = HttpClient(Apache, config)

    val accessTokenClient =
        AccessTokenClient(env.aadAccessTokenUrl, env.clientId, env.clientSecret, httpClient)
    val pdlClient =
        PdlClient(
            httpClient,
            env.pdlGraphqlPath,
            PdlClient::class
                .java
                .getResource("/graphql/getPerson.graphql")!!
                .readText()
                .replace(Regex("[\n\t]"), ""),
        )
    val pdlPersonService = PdlPersonService(pdlClient, accessTokenClient, env.pdlScope)

    val dokarkivClient =
        DokarkivClient(
            url = env.dokarkivUrl,
            accessTokenClient = accessTokenClient,
            scope = env.dokarkivScope,
            httpClient = httpClient,
        )

    val syfosmregisterClient =
        SyfosmregisterClient(
            syfosmregisterUrl = env.syfosmregisterUrl,
            accessTokenClient = accessTokenClient,
            syfosmregisterScope = env.syfosmregisterScope,
            httpClient = httpClient,
        )

    val syfosmreglerClient =
        SyfosmreglerClient(
            syfosmreglerUrl = env.syfosmreglerUrl,
            accessTokenClient = accessTokenClient,
            syfosmreglerScope = env.syfosmreglerScope,
            httpClient = httpClient,
        )

    val syfosmpapirreglerClient =
        SyfosmpapirreglerClient(
            syfosmpapirreglerUrl = env.syfosmpapirreglerUrl,
            accessTokenClient = accessTokenClient,
            syfosmpapirreglerScope = env.syfosmpapirreglerScope,
            httpClient = httpClient,
        )

    val norskHelsenettClient =
        NorskHelsenettClient(
            norskHelsenettUrl = env.norskHelsenettUrl,
            accessTokenClient = accessTokenClient,
            norskHelsenettScope = env.norskHelsenettScope,
            httpClient = httpClient,
        )

    val oppgaveClient =
        OppgaveClient(
            url = env.oppgaveUrl,
            accessTokenClient = accessTokenClient,
            scope = env.oppgaveScope,
            httpClient = httpClient,
        )

    val producerProperties =
        KafkaUtils.getAivenKafkaConfig("nl-response-producer")
            .toProducerConfig(
                env.applicationName,
                JacksonKafkaSerializer::class,
                StringSerializer::class
            )

    val kafkaProducer = KafkaProducer<String, NlResponseKafkaMessage>(producerProperties)
    val nlResponseKafkaProducer = NlResponseProducer(kafkaProducer, env.narmestelederTopic)

    val tombstoneProducer =
        KafkaProducer<String, Any?>(
            KafkaUtils.getAivenKafkaConfig("tombstone-producer")
                .toProducerConfig("env.applicationName", JacksonNullableKafkaSerializer::class),
        )
    val tombstoneKafkaProducer =
        TombstoneKafkaProducer(
            tombstoneProducer,
            listOf(env.papirSmRegistreringTopic, env.manuellTopic)
        )
    val sykmeldingStatusKafkaProducer =
        SykmeldingStatusKafkaProducer(KafkaProducer(producerProperties), env.sykmeldingStatusTopic)

    val narmestelederService = NarmestelederService(nlResponseKafkaProducer, pdlPersonService)
    val slettSykmeldingService =
        SlettSykmeldingService(
            syfosmregisterClient,
            sykmeldingStatusKafkaProducer,
            tombstoneKafkaProducer
        )
    val sykmeldingService =
        SykmeldingService(pdlPersonService, connection, env.sykmeldingQueue, syfosmreglerClient)
    val legeerklaeringService =
        LegeerklaeringService(pdlPersonService, connection, env.legeerklaeringQueue)
    val papirsykmeldingService =
        PapirsykmeldingService(dokarkivClient, syfosmpapirreglerClient, norskHelsenettClient)
    val utenlandskSykmeldingService = UtenlandskSykmeldingService(dokarkivClient, oppgaveClient)

    configureRouting(
        env,
        applicationState,
        narmestelederService,
        sykmeldingService,
        slettSykmeldingService,
        legeerklaeringService,
        papirsykmeldingService,
        utenlandskSykmeldingService,
    )
}

fun Application.configureRouting(
    environmentVariables: EnvironmentVariables,
    applicationState: ApplicationState,
    narmestelederService: NarmestelederService,
    sykmeldingService: SykmeldingService,
    slettSykmeldingService: SlettSykmeldingService,
    legeerklaeringService: LegeerklaeringService,
    papirsykmeldingService: PapirsykmeldingService,
    utenlandskSykmeldingService: UtenlandskSykmeldingService,
) {
    install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
        jackson {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    install(CallId) {
        generate { UUID.randomUUID().toString() }
        verify { callId: String -> callId.isNotEmpty() }
        header(HttpHeaders.XCorrelationId)
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Caught exception", cause)
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unknown error")
        }
    }

    routing {
        if (environmentVariables.clusterName == "dev-gcp") {
            staticResources("/api/v1/docs/", "api") { default("api/index.html") }
        }
        naisIsAliveRoute(applicationState)
        naisIsReadyRoute(applicationState)
        naisPrometheusRoute()
        registrerNarmestelederApi(narmestelederService)
        registrerSykmeldingApi(sykmeldingService, slettSykmeldingService)
        registrerLegeerklaeringApi(legeerklaeringService)
        registrerPapirsykmeldingApi(papirsykmeldingService)
        registrerUtenlandskPapirsykmeldingApi(utenlandskSykmeldingService)
    }
    intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests())
}

data class ApplicationState(
    var alive: Boolean = true,
    var ready: Boolean = true,
)

data class HttpMessage(
    val message: String,
)

class ServiceUnavailableException(message: String?) : Exception(message)
