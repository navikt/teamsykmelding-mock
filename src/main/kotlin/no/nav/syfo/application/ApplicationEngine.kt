package no.nav.syfo.application

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import no.nav.syfo.Environment
import no.nav.syfo.application.api.registerNaisApi
import no.nav.syfo.application.api.setupSwaggerDocApi
import no.nav.syfo.application.metrics.monitorHttpRequests
import no.nav.syfo.legeerklaering.LegeerklaeringService
import no.nav.syfo.legeerklaering.api.registrerLegeerklaeringApi
import no.nav.syfo.log
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.narmesteleder.api.registrerNarmestelederApi
import no.nav.syfo.papirsykmelding.PapirsykmeldingService
import no.nav.syfo.papirsykmelding.api.registrerPapirsykmeldingApi
import no.nav.syfo.sykmelding.SlettSykmeldingService
import no.nav.syfo.sykmelding.SykmeldingService
import no.nav.syfo.sykmelding.api.registrerSykmeldingApi
import no.nav.syfo.utenlandsk.api.registrerUtenlandskPapirsykmeldingApi
import no.nav.syfo.utenlandsk.opprettJournalpostservice.UtenlandskSykmeldingService
import java.util.UUID

fun createApplicationEngine(
    env: Environment,
    applicationState: ApplicationState,
    narmestelederService: NarmestelederService,
    sykmeldingService: SykmeldingService,
    slettSykmeldingService: SlettSykmeldingService,
    legeerklaeringService: LegeerklaeringService,
    papirsykmeldingService: PapirsykmeldingService,
    utenlandskSykmeldingService: UtenlandskSykmeldingService,
): ApplicationEngine =
    embeddedServer(Netty, env.applicationPort) {
        install(ContentNegotiation) {
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
                log.error("Caught exception", cause)
                call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unknown error")
            }
        }

        routing {
            registerNaisApi(applicationState)
            setupSwaggerDocApi()
            registrerNarmestelederApi(narmestelederService)
            registrerSykmeldingApi(sykmeldingService, slettSykmeldingService)
            registrerLegeerklaeringApi(legeerklaeringService)
            registrerPapirsykmeldingApi(papirsykmeldingService)
            registrerUtenlandskPapirsykmeldingApi(utenlandskSykmeldingService)
        }
        intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests())
    }
