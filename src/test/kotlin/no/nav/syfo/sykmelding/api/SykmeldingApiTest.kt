package no.nav.syfo.sykmelding.api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.headers
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.sm.Diagnosekoder.objectMapper
import no.nav.syfo.sykmelding.SlettSykmeldingService
import no.nav.syfo.sykmelding.SykmeldingService
import no.nav.syfo.utils.generateJWT
import no.nav.syfo.utils.setupTestApplication
import no.nav.syfo.utils.testClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.koin.dsl.module

internal class SykmeldingApiTest {
    val sykmeldingService = mockk<SykmeldingService>()
    val slettSykmeldingService = mockk<SlettSykmeldingService>()

    val validationResult =
        ValidationResult(
            Status.INVALID,
            listOf(
                RuleInfo(
                    ruleName = "BEHANDLER_IKKE_GYLDIG_I_HPR",
                    messageForSender =
                        "Behandler er ikke gyldig i HPR på konsultasjonstidspunkt. Pasienten har fått beskjed.",
                    messageForUser = "Den som skrev sykmeldingen manglet autorisasjon.",
                    ruleStatus = Status.INVALID,
                ),
            ),
        )

    @AfterEach fun cleanup() = stopKoin()

    @Test
    internal fun `Creating sykmelding with bidiagnoser`() = testApplication {
        setupTestApplication {
            dependencies { modules(module { single { sykmeldingService } }) }
            authedRoutes { registrerSykmeldingApi() }
        }

        coEvery { sykmeldingService.opprettSykmelding(any()) } returns "123-123--21321313"
        coEvery { sykmeldingService.sjekkRegler(any()) } returns validationResult
        val response =
            testClient().post("/sykmelding/opprett") {
                headers {
                    append("Content-Type", ContentType.Application.Json.toString())
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                }
                setBody(
                    "{\n" +
                        "  \"syketilfelleStartdato\": \"2022-09-27\",\n" +
                        "  \"behandletDato\": \"2022-09-27\",\n" +
                        "  \"perioder\": [\n" +
                        "    {\n" +
                        "      \"fom\": \"2022-09-27\",\n" +
                        "      \"tom\": \"2022-10-03\",\n" +
                        "      \"type\": \"HUNDREPROSENT\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"hoveddiagnose\": {\n" +
                        "    \"code\": \"A90\",\n" +
                        "    \"text\": \"Medfødt feil IKA/multiple feil\",\n" +
                        "    \"system\": \"ICPC2\"\n" +
                        "  },\n" +
                        "  \"fnr\": \"20086600138\",\n" +
                        "  \"fnrLege\": \"01117302624\",\n" +
                        "  \"herId\": null,\n" +
                        "  \"hprNummer\": \"7125186\",\n" +
                        "  \"meldingTilArbeidsgiver\": null,\n" +
                        "  \"annenFraverGrunn\": null,\n" +
                        "  \"begrunnIkkeKontakt\": null,\n" +
                        "  \"vedlegg\": false,\n" +
                        "  \"virksomhetsykmelding\": false,\n" +
                        "  \"utenUtdypendeOpplysninger\": false,\n" +
                        "  \"regelsettVersjon\": \"2\",\n" +
                        "  \"kontaktDato\": null,\n" +
                        "  \"bidiagnoser\": [\n" +
                        "    {\n" +
                        "      \"code\": \"Z999\",\n" +
                        "      \"text\": \"Avhengighet av ikke spes. teknisk hjelpemiddel og innretning\",\n" +
                        "      \"system\": \"ICD10\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"diagnosekodesystem\": \"ICPC2\",\n" +
                        "  \"diagnosekode\": \"A90\",\n" +
                        "  \"arbeidsgiverNavn\": null\n" +
                        "}",
                )
            }

        assertEquals(response.status, HttpStatusCode.OK)

        val responseBody = response.bodyAsText()
        val httpMessage = objectMapper.readValue(responseBody, HttpMessage::class.java)
        assertEquals("Opprettet sykmelding med mottakId 123-123--21321313", httpMessage.message)
    }

    @Test
    internal fun `Creating sykmelding with empty bidiagnoser`() = testApplication {
        setupTestApplication {
            dependencies { modules(module { single { sykmeldingService } }) }
            authedRoutes { registrerSykmeldingApi() }
        }

        coEvery { sykmeldingService.opprettSykmelding(any()) } returns "123-123--21321313"
        coEvery { sykmeldingService.sjekkRegler(any()) } returns validationResult
        val response =
            testClient().post("/sykmelding/opprett") {
                headers {
                    append("Content-Type", ContentType.Application.Json.toString())
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                }
                setBody(
                    "{\n" +
                        "  \"syketilfelleStartdato\": \"2022-09-27\",\n" +
                        "  \"behandletDato\": \"2022-09-27\",\n" +
                        "  \"perioder\": [\n" +
                        "    {\n" +
                        "      \"fom\": \"2022-09-27\",\n" +
                        "      \"tom\": \"2022-10-03\",\n" +
                        "      \"type\": \"HUNDREPROSENT\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"hoveddiagnose\": {\n" +
                        "    \"code\": \"A90\",\n" +
                        "    \"text\": \"Medfødt feil IKA/multiple feil\",\n" +
                        "    \"system\": \"ICPC2\"\n" +
                        "  },\n" +
                        "  \"fnr\": \"20086600138\",\n" +
                        "  \"fnrLege\": \"01117302624\",\n" +
                        "  \"herId\": null,\n" +
                        "  \"hprNummer\": \"7125186\",\n" +
                        "  \"meldingTilArbeidsgiver\": null,\n" +
                        "  \"annenFraverGrunn\": null,\n" +
                        "  \"begrunnIkkeKontakt\": null,\n" +
                        "  \"vedlegg\": false,\n" +
                        "  \"virksomhetsykmelding\": false,\n" +
                        "  \"utenUtdypendeOpplysninger\": false,\n" +
                        "  \"regelsettVersjon\": \"2\",\n" +
                        "  \"kontaktDato\": null,\n" +
                        "  \"bidiagnoser\": [],\n" +
                        "  \"diagnosekodesystem\": \"ICPC2\",\n" +
                        "  \"diagnosekode\": \"A90\",\n" +
                        "  \"arbeidsgiverNavn\": \"NAV\"\n" +
                        "}",
                )
            }

        assertEquals(response.status, HttpStatusCode.OK)

        val responseBody = response.bodyAsText()
        val httpMessage = objectMapper.readValue(responseBody, HttpMessage::class.java)
        assertEquals("Opprettet sykmelding med mottakId 123-123--21321313", httpMessage.message)
    }

    @Test
    internal fun `Regelsjekk`() = testApplication {
        setupTestApplication {
            dependencies { modules(module { single { sykmeldingService } }) }
            authedRoutes { registrerSykmeldingApi() }
        }

        coEvery { sykmeldingService.opprettSykmelding(any()) } returns "123-123--21321313"
        coEvery { sykmeldingService.sjekkRegler(any()) } returns validationResult

        val response =
            testClient().post("/sykmelding/regelsjekk") {
                headers {
                    append("Content-Type", ContentType.Application.Json.toString())
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                }
                setBody(
                    "{\n" +
                        "  \"syketilfelleStartdato\": \"2022-09-27\",\n" +
                        "  \"behandletDato\": \"2022-09-27\",\n" +
                        "  \"perioder\": [\n" +
                        "    {\n" +
                        "      \"fom\": \"2022-09-27\",\n" +
                        "      \"tom\": \"2022-10-03\",\n" +
                        "      \"type\": \"HUNDREPROSENT\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"hoveddiagnose\": {\n" +
                        "    \"code\": \"A90\",\n" +
                        "    \"text\": \"Medfødt feil IKA/multiple feil\",\n" +
                        "    \"system\": \"ICPC2\"\n" +
                        "  },\n" +
                        "  \"fnr\": \"20086600138\",\n" +
                        "  \"fnrLege\": \"01117302624\",\n" +
                        "  \"herId\": null,\n" +
                        "  \"hprNummer\": \"7125186\",\n" +
                        "  \"meldingTilArbeidsgiver\": null,\n" +
                        "  \"annenFraverGrunn\": null,\n" +
                        "  \"begrunnIkkeKontakt\": null,\n" +
                        "  \"vedlegg\": false,\n" +
                        "  \"virksomhetsykmelding\": false,\n" +
                        "  \"utenUtdypendeOpplysninger\": false,\n" +
                        "  \"regelsettVersjon\": \"2\",\n" +
                        "  \"kontaktDato\": null,\n" +
                        "  \"bidiagnoser\": [\n" +
                        "    {\n" +
                        "      \"code\": \"Z999\",\n" +
                        "      \"text\": \"Avhengighet av ikke spes. teknisk hjelpemiddel og innretning\",\n" +
                        "      \"system\": \"ICD10\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"diagnosekodesystem\": \"ICPC2\",\n" +
                        "  \"diagnosekode\": \"A90\",\n" +
                        "  \"arbeidsgiverNavn\": null\n" +
                        "}",
                )
            }

        assertEquals(response.status, HttpStatusCode.OK)
        val responseBody = response.bodyAsText()
        val validationResultFromResponse =
            objectMapper.readValue(responseBody, ValidationResult::class.java)
        assertEquals(validationResult, validationResultFromResponse)
    }
}
