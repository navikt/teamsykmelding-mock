package no.nav.syfo.sykmelding.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.syfo.ApplicationState
import no.nav.syfo.HttpMessage
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.objectMapper
import no.nav.syfo.sykmelding.SlettSykmeldingService
import no.nav.syfo.sykmelding.SykmeldingService
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

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

    @Test
    internal fun `Creating sykmelding with bidiagnoser`() {
        coEvery { sykmeldingService.opprettSykmelding(any()) } returns "123-123--21321313"
        coEvery { sykmeldingService.sjekkRegler(any()) } returns validationResult

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
            application.routing {
                registrerSykmeldingApi(
                    sykmeldingService,
                    slettSykmeldingService,
                )
            }

            with(
                handleRequest(HttpMethod.Post, "/sykmelding/opprett") {
                    addHeader("Content-Type", ContentType.Application.Json.toString())
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
                },
            ) {
                response.status() shouldBeEqualTo HttpStatusCode.OK
                response.content shouldBeEqualTo
                    objectMapper.writeValueAsString(
                        HttpMessage("Opprettet sykmelding med mottakId 123-123--21321313"),
                    )
            }
        }
    }

    @Test
    internal fun `Creating sykmelding with empty bidiagnoser`() {
        coEvery { sykmeldingService.opprettSykmelding(any()) } returns "123-123--21321313"
        coEvery { sykmeldingService.sjekkRegler(any()) } returns validationResult

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
            application.routing {
                registrerSykmeldingApi(
                    sykmeldingService,
                    slettSykmeldingService,
                )
            }

            with(
                handleRequest(HttpMethod.Post, "/sykmelding/opprett") {
                    addHeader("Content-Type", ContentType.Application.Json.toString())
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
                },
            ) {
                response.status() shouldBeEqualTo HttpStatusCode.OK
                response.content shouldBeEqualTo
                    objectMapper.writeValueAsString(
                        HttpMessage("Opprettet sykmelding med mottakId 123-123--21321313"),
                    )
            }
        }
    }

    @Test
    internal fun `Regelsjekk`() {
        coEvery { sykmeldingService.opprettSykmelding(any()) } returns "123-123--21321313"
        coEvery { sykmeldingService.sjekkRegler(any()) } returns validationResult

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
            application.routing {
                registrerSykmeldingApi(
                    sykmeldingService,
                    slettSykmeldingService,
                )
            }

            with(
                handleRequest(HttpMethod.Post, "/sykmelding/regelsjekk") {
                    addHeader("Content-Type", ContentType.Application.Json.toString())
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
                },
            ) {
                response.status() shouldBeEqualTo HttpStatusCode.OK
                response.content shouldBeEqualTo objectMapper.writeValueAsString(validationResult)
            }
        }
    }
}
