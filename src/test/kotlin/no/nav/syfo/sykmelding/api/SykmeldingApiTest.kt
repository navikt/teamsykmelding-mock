package no.nav.syfo.sykmelding.api

import io.kotest.core.spec.style.FunSpec
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.objectMapper
import no.nav.syfo.sykmelding.SlettSykmeldingService
import no.nav.syfo.sykmelding.SykmeldingService
import no.nav.syfo.withKtor
import org.amshove.kluent.shouldBeEqualTo

class SykmeldingApiTest : FunSpec({

    val sykmeldingService = mockk<SykmeldingService>()
    val slettSykmeldingService = mockk<SlettSykmeldingService>()

    val validationResult = ValidationResult(
        Status.INVALID,
        listOf(
            RuleInfo(
                ruleName = "BEHANDLER_IKKE_GYLDIG_I_HPR",
                messageForSender = "Behandler er ikke gyldig i HPR på konsultasjonstidspunkt. Pasienten har fått beskjed.",
                messageForUser = "Den som skrev sykmeldingen manglet autorisasjon.",
                ruleStatus = Status.INVALID
            )
        )
    )

    coEvery { sykmeldingService.opprettSykmelding(any()) } returns "123-123--21321313"
    coEvery { sykmeldingService.sjekkRegler(any()) } returns validationResult

    withKtor({
        registrerSykmeldingApi(sykmeldingService, slettSykmeldingService)
    }) {
        context("SykmeldingApi") {
            test("Creating sykmelding with bidiagnoser") {
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
                                "}"
                        )
                    }
                ) {
                    response.status() shouldBeEqualTo HttpStatusCode.OK
                    response.content shouldBeEqualTo objectMapper.writeValueAsString(HttpMessage("Opprettet sykmelding med mottakId 123-123--21321313"))
                }
            }

            test("Creating sykmelding with empty bidiagnoser") {
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
                                "}"
                        )
                    }
                ) {
                    response.status() shouldBeEqualTo HttpStatusCode.OK
                    response.content shouldBeEqualTo objectMapper.writeValueAsString(HttpMessage("Opprettet sykmelding med mottakId 123-123--21321313"))
                }
            }

            test("Regelsjekk") {
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
                                "}"
                        )
                    }
                ) {
                    response.status() shouldBeEqualTo HttpStatusCode.OK
                    response.content shouldBeEqualTo objectMapper.writeValueAsString(validationResult)
                }
            }
        }
    }
})
