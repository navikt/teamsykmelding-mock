package no.nav.syfo.narmesteleder.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.azuread.AccessTokenClient
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.narmesteleder.kafka.NlResponseProducer
import no.nav.syfo.narmesteleder.kafka.model.Leder
import no.nav.syfo.narmesteleder.kafka.model.NlResponse
import no.nav.syfo.narmesteleder.kafka.model.Sykmeldt
import no.nav.syfo.objectMapper
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.model.GetPersonResponse
import no.nav.syfo.pdl.client.model.HentPersonBolk
import no.nav.syfo.pdl.client.model.Navn
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.pdl.client.model.ResponseData
import no.nav.syfo.pdl.service.PdlPersonService
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class NarmestelederApiKtTest {
    val nlResponseProducer = mockk<NlResponseProducer>(relaxed = true)
    val pdlClient = mockk<PdlClient>()
    val accessTokenClient = mockk<AccessTokenClient>()
    val pdlPersonService = PdlPersonService(pdlClient, accessTokenClient, "scope")
    val narmestelederService = NarmestelederService(nlResponseProducer, pdlPersonService)

    val ansattFnr = "12345678910"
    val lederFnr = "10987654321"
    val orgnummer = "888888888"

    @BeforeEach
    internal fun `Set up`() {
        clearMocks(nlResponseProducer, pdlClient, accessTokenClient)
        coEvery { accessTokenClient.getAccessToken(any()) } returns "token"
    }

    @Test
    internal fun `Oppretter NL-kobling`() {
        with(TestApplicationEngine()) {
            start()
            val applicationState = ApplicationState()
            applicationState.ready = true
            applicationState.alive = true
            application.install(ContentNegotiation) {
                jackson {
                    registerKotlinModule()
                    registerModule(JavaTimeModule())
                    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
            application.routing { registrerNarmestelederApi(narmestelederService) }

            coEvery { pdlClient.getPersoner(any(), any()) } returns
                GetPersonResponse(
                    ResponseData(
                        listOf(
                            HentPersonBolk(
                                ansattFnr,
                                Person(listOf(Navn("Fornavn", null, "Etternavn"))),
                                "ok",
                            ),
                            HentPersonBolk(
                                lederFnr,
                                Person(listOf(Navn("Leder", null, "Ledersen"))),
                                "ok",
                            ),
                        ),
                    ),
                    null,
                )
            val opprettNarmestelederRequest =
                OpprettNarmestelederRequest(
                    ansattFnr = ansattFnr,
                    lederFnr = lederFnr,
                    orgnummer = orgnummer,
                    mobil = "98989898",
                    epost = "test@nav.no",
                    forskutterer = true,
                    aktivFom = LocalDate.now(),
                )
            with(
                handleRequest(HttpMethod.Post, "/narmesteleder/opprett") {
                    addHeader("Content-Type", ContentType.Application.Json.toString())
                    setBody(objectMapper.writeValueAsString(opprettNarmestelederRequest))
                },
            ) {
                response.status() shouldBeEqualTo HttpStatusCode.OK
                response.content shouldBeEqualTo
                    objectMapper.writeValueAsString(
                        HttpMessage("Nærmeste leder er registrert"),
                    )
            }
            coVerify {
                nlResponseProducer.sendNlResponse(
                    match {
                        it ==
                            NlResponse(
                                orgnummer = orgnummer,
                                utbetalesLonn = true,
                                leder =
                                    Leder(
                                        fnr = lederFnr,
                                        mobil = "98989898",
                                        epost = "test@nav.no",
                                        fornavn = "Leder",
                                        etternavn = "Ledersen",
                                    ),
                                sykmeldt =
                                    Sykmeldt(
                                        fnr = ansattFnr,
                                        navn = "Fornavn Etternavn",
                                    ),
                                aktivFom =
                                    OffsetDateTime.of(
                                        LocalDate.now().atStartOfDay(),
                                        ZoneOffset.UTC,
                                    ),
                            )
                    },
                    nlAvbrutt = null,
                )
            }
        }
    }

    @Test
    internal fun `Deaktiverer NL-kobling`() {
        with(TestApplicationEngine()) {
            start()
            val applicationState = ApplicationState()
            applicationState.ready = true
            applicationState.alive = true
            application.install(ContentNegotiation) {
                jackson {
                    registerKotlinModule()
                    registerModule(JavaTimeModule())
                    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
            application.routing { registrerNarmestelederApi(narmestelederService) }
            with(
                handleRequest(HttpMethod.Delete, "/narmesteleder/$orgnummer") {
                    addHeader("Sykmeldt-Fnr", ansattFnr)
                },
            ) {
                response.status() shouldBeEqualTo HttpStatusCode.OK
            }
            coVerify {
                nlResponseProducer.sendNlResponse(
                    nlResponse = null,
                    match { it.orgnummer == orgnummer && it.sykmeldtFnr == ansattFnr },
                )
            }
        }
    }

    @Test
    internal fun `Deaktivering gir 400 hvis fnr mangler`() {
        with(TestApplicationEngine()) {
            start()
            val applicationState = ApplicationState()
            applicationState.ready = true
            applicationState.alive = true
            application.install(ContentNegotiation) {
                jackson {
                    registerKotlinModule()
                    registerModule(JavaTimeModule())
                    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
            application.routing { registrerNarmestelederApi(narmestelederService) }
            with(
                handleRequest(HttpMethod.Delete, "/narmesteleder/$orgnummer"),
            ) {
                response.status() shouldBeEqualTo HttpStatusCode.BadRequest
            }
            coVerify(exactly = 0) { nlResponseProducer.sendNlResponse(any(), any()) }
        }
    }
}
