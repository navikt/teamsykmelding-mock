package no.nav.syfo.narmesteleder.api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.headers
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.narmesteleder.kafka.NlResponseProducer
import no.nav.syfo.narmesteleder.kafka.model.Leder
import no.nav.syfo.narmesteleder.kafka.model.NlResponse
import no.nav.syfo.narmesteleder.kafka.model.Sykmeldt
import no.nav.syfo.pdl.model.Navn
import no.nav.syfo.pdl.model.PdlPerson
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.sm.Diagnosekoder.objectMapper
import no.nav.syfo.utils.generateJWT
import no.nav.syfo.utils.setupTestApplication
import no.nav.syfo.utils.testClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.koin.dsl.module

internal class NarmestelederApiKtTest {
    val nlResponseProducer = mockk<NlResponseProducer>(relaxed = true)
    val pdlPersonService = mockk<PdlPersonService>(relaxed = true)

    val ansattFnr = "12345678910"
    val lederFnr = "10987654321"
    val orgnummer = "888888888"

    @AfterEach fun cleanup() = stopKoin()

    @Test
    internal fun `Oppretter NL-kobling`() = testApplication {
        val narmestelederService = NarmestelederService(nlResponseProducer, pdlPersonService)
        setupTestApplication {
            dependencies { modules(module { single { narmestelederService } }) }
            authedRoutes { registrerNarmestelederApi() }
        }
        coEvery { pdlPersonService.getPersoner(any()) } returns
            mapOf(
                ansattFnr to PdlPerson(Navn("Fornavn", null, "Etternavn")),
                lederFnr to PdlPerson(Navn("Leder", null, "Ledersen")),
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

        val response =
            testClient().post("/narmesteleder/opprett") {
                headers {
                    append("Content-Type", "application/json")
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                }
                setBody(objectMapper.writeValueAsString(opprettNarmestelederRequest))
            }

        assertEquals(response.status, HttpStatusCode.OK)

        val responseBody = response.bodyAsText()
        val httpMessage = objectMapper.readValue(responseBody, HttpMessage::class.java)
        assertEquals("Nærmeste leder er registrert", httpMessage.message)

        coVerify {
            println("Verifiserer at sendNlResponse blir kalt...")
            val expectedNlResponse =
                NlResponse(
                    orgnummer = orgnummer,
                    utbetalesLonn = true,
                    leder =
                        Leder(
                            fnr = lederFnr,
                            mobil = "98989898",
                            epost = "test@nav.no",
                            fornavn = "Leder",
                            etternavn = "Ledersen"
                        ),
                    sykmeldt = Sykmeldt(fnr = ansattFnr, navn = "Fornavn Etternavn"),
                    aktivFom = OffsetDateTime.of(LocalDate.now().atStartOfDay(), ZoneOffset.UTC)
                )
            println("Forventet NlResponse: $expectedNlResponse")
            nlResponseProducer.sendNlResponse(
                match { actualNlResponse ->
                    println("Matcher NlResponse: $actualNlResponse")
                    actualNlResponse ==
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

    @Test
    internal fun `Deaktiverer NL-kobling`() = testApplication {
        val narmestelederService = NarmestelederService(nlResponseProducer, pdlPersonService)
        setupTestApplication {
            dependencies { modules(module { single { narmestelederService } }) }
            authedRoutes { registrerNarmestelederApi() }
        }
        coEvery { pdlPersonService.getPersoner(any()) } returns
            mapOf(
                ansattFnr to PdlPerson(Navn("Fornavn", null, "Etternavn")),
                lederFnr to PdlPerson(Navn("Leder", null, "Ledersen")),
            )

        val response =
            testClient().delete("/narmesteleder/$orgnummer") {
                headers {
                    append("Content-Type", "application/json")
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                    append("Sykmeldt-Fnr", ansattFnr)
                }
            }

        assertEquals(response.status, HttpStatusCode.OK)
        coVerify {
            nlResponseProducer.sendNlResponse(
                nlResponse = null,
                match { it.orgnummer == orgnummer && it.sykmeldtFnr == ansattFnr },
            )
        }
    }

    @Test
    internal fun `Deaktivering gir 400 hvis fnr mangler`() = testApplication {
        val narmestelederService = NarmestelederService(nlResponseProducer, pdlPersonService)
        setupTestApplication {
            dependencies { modules(module { single { narmestelederService } }) }
            authedRoutes { registrerNarmestelederApi() }
        }
        val response =
            testClient().delete("/narmesteleder/$orgnummer") {
                headers {
                    append("Content-Type", "application/json")
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                }
            }
        assertEquals(response.status, HttpStatusCode.BadRequest)
        coVerify(exactly = 0) { nlResponseProducer.sendNlResponse(any(), any()) }
    }
}
