package no.nav.syfo.sykmelding.api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.Status
import no.nav.syfo.model.SykmeldingPeriode
import no.nav.syfo.model.SykmeldingType
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.sykmelding.SykmeldingService
import no.nav.syfo.sykmelding.model.Diagnose
import no.nav.syfo.sykmelding.model.SykmeldingRequest
import no.nav.syfo.utils.setupTestApplication
import no.nav.syfo.utils.testClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.time.LocalDate

internal class SykmeldingApiTest {
    val objectMapper = jacksonMapperBuilder().build()

    val sykmeldingService = mockk<SykmeldingService>()

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
                )
            ),
        )

    @AfterEach fun cleanup() = stopKoin()

    @Test
    internal fun `Creating sykmelding with bidiagnoser`() = testApplication {
        setupTestApplication {
            dependencies { modules(module { single { sykmeldingService } }) }
            openRoutes { registrerSykmeldingApi() }
        }

        coEvery { sykmeldingService.opprettSykmelding(any()) } returns "123-123--21321313"
        coEvery { sykmeldingService.sjekkRegler(any()) } returns validationResult
        val sykmeldingRequest =
            SykmeldingRequest(
                fnr = "04827695713",
                fnrLege = "01117302624",
                herId = null,
                hprNummer = "7125186",
                syketilfelleStartdato = LocalDate.of(2022,9,27),
                annenFraverGrunn = null,
                perioder =
                    listOf(
                        SykmeldingPeriode(
                            fom = LocalDate.of(2022,9,27),
                            tom = LocalDate.of(2022,10,3),
                            type = SykmeldingType.HUNDREPROSENT,
                        )
                    ),
                behandletDato = LocalDate.of(2022,9,27),
                kontaktDato = null,
                begrunnIkkeKontakt = null,
                vedlegg = true,
                virksomhetsykmelding = false,
                utdypendeOpplysninger = null,
                regelsettVersjon = "2",
                meldingTilArbeidsgiver = null,
                bidiagnoser = listOf(
                    Diagnose(code = "Z999", system = "ICD10", text = "Avhengighet av ikke spes. teknisk hjelpemiddel og innretning"),
                ),
                arbeidsgiverNavn = null,
                vedleggMedVirus = false,
                beskrivBistandNav = null,
                yrkesskade = false,
                hoveddiagnose = Diagnose(code = "A90", system = "ICPC2", text = "Medfødt feil IKA/multiple feil"),
            )

        val response =
            testClient().post("/sykmelding/opprett") {
                headers { append("Content-Type", ContentType.Application.Json.toString()) }
                setBody(objectMapper.writeValueAsString(sykmeldingRequest))
            }

         assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.bodyAsText()
        val httpMessage = objectMapper.readValue(responseBody, HttpMessage::class.java)
        assertEquals("Opprettet sykmelding med mottakId 123-123--21321313", httpMessage.message)
    }

    @Test
    internal fun `Creating sykmelding with empty bidiagnoser`() = testApplication {
        setupTestApplication {
            dependencies { modules(module { single { sykmeldingService } }) }
            openRoutes { registrerSykmeldingApi() }
        }

        coEvery { sykmeldingService.opprettSykmelding(any()) } returns "123-123--21321313"
        coEvery { sykmeldingService.sjekkRegler(any()) } returns validationResult

        val sykmeldingRequest =
            SykmeldingRequest(
                fnr = "04827695713",
                fnrLege = "01117302624",
                herId = null,
                hprNummer = "7125186",
                syketilfelleStartdato = LocalDate.of(2022,9,27),
                annenFraverGrunn = null,
                perioder =
                    listOf(
                        SykmeldingPeriode(
                            fom = LocalDate.of(2022,9,27),
                            tom = LocalDate.of(2022,10,3),
                            type = SykmeldingType.HUNDREPROSENT,
                        )
                    ),
                behandletDato = LocalDate.of(2022,9,27),
                kontaktDato = null,
                begrunnIkkeKontakt = null,
                vedlegg = true,
                virksomhetsykmelding = false,
                utdypendeOpplysninger = null,
                regelsettVersjon = "2",
                meldingTilArbeidsgiver = null,
                bidiagnoser = emptyList(),
                arbeidsgiverNavn = null,
                vedleggMedVirus = false,
                beskrivBistandNav = null,
                yrkesskade = false,
                hoveddiagnose = Diagnose(code = "A90", system = "ICPC2", text = "Medfødt feil IKA/multiple feil"),
            )
        val response =
            testClient().post("/sykmelding/opprett") {
                headers { append("Content-Type", ContentType.Application.Json.toString()) }
                setBody(objectMapper.writeValueAsString(sykmeldingRequest))
            }

         assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.bodyAsText()
        val httpMessage = objectMapper.readValue(responseBody, HttpMessage::class.java)
        assertEquals("Opprettet sykmelding med mottakId 123-123--21321313", httpMessage.message)
    }

    @Test
    internal fun `Regelsjekk`() = testApplication {
        setupTestApplication {
            dependencies { modules(module { single { sykmeldingService } }) }
            openRoutes { registrerSykmeldingApi() }
        }

        coEvery { sykmeldingService.opprettSykmelding(any()) } returns "123-123--21321313"
        coEvery { sykmeldingService.sjekkRegler(any()) } returns validationResult

        val sykmeldingRequest =
            SykmeldingRequest(
                fnr = "04827695713",
                fnrLege = "01117302624",
                herId = null,
                hprNummer = "7125186",
                syketilfelleStartdato = LocalDate.of(2022,9,27),
                annenFraverGrunn = null,
                perioder =
                    listOf(
                        SykmeldingPeriode(
                            fom = LocalDate.of(2022,9,27),
                            tom = LocalDate.of(2022,10,3),
                            type = SykmeldingType.HUNDREPROSENT,
                        )
                    ),
                behandletDato = LocalDate.of(2022,9,27),
                kontaktDato = null,
                begrunnIkkeKontakt = null,
                vedlegg = false,
                virksomhetsykmelding = false,
                utdypendeOpplysninger = null,
                regelsettVersjon = "2",
                meldingTilArbeidsgiver = null,
                bidiagnoser = listOf(
                    Diagnose(code = "Z999", system = "ICD10", text = "Avhengighet av ikke spes. teknisk hjelpemiddel og innretning"),
                ),
                arbeidsgiverNavn = null,
                vedleggMedVirus = false,
                beskrivBistandNav = null,
                yrkesskade = false,
                hoveddiagnose = Diagnose(code = "A90", system = "ICPC2", text = "Medfødt feil IKA/multiple feil"),
            )

        val response =
            testClient().post("/sykmelding/regelsjekk") {
                headers { append("Content-Type", ContentType.Application.Json.toString()) }
                setBody(objectMapper.writeValueAsString(sykmeldingRequest))
            }

         assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        val validationResultFromResponse =
            objectMapper.readValue(responseBody, ValidationResult::class.java)
        assertEquals(validationResult, validationResultFromResponse)
    }
}
