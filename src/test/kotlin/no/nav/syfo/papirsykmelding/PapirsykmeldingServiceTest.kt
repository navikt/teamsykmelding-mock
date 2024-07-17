package no.nav.syfo.papirsykmelding

import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.math.BigInteger
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import no.nav.syfo.model.SykmeldingPeriode
import no.nav.syfo.model.SykmeldingType
import no.nav.syfo.papirsykmelding.client.DokarkivClient
import no.nav.syfo.papirsykmelding.client.NorskHelsenettClient
import no.nav.syfo.papirsykmelding.client.SyfosmpapirreglerClient
import no.nav.syfo.papirsykmelding.model.PapirsykmeldingRequest
import no.nav.syfo.utils.setupTestApplication
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.koin.dsl.module

internal class PapirsykmeldingServiceTest {
    private val dokarkivClient = mockk<DokarkivClient>()
    private val syfosmpapirreglerClient = mockk<SyfosmpapirreglerClient>()
    private val norskHelsenettClient = mockk<NorskHelsenettClient>()
    private val papirsykmeldingService =
        PapirsykmeldingService(dokarkivClient, syfosmpapirreglerClient, norskHelsenettClient)
    private val fnr = "1234567890"

    val papirsykmeldingRequest =
        PapirsykmeldingRequest(
            fnr = fnr,
            hprNummer = "7125186",
            syketilfelleStartdato = LocalDate.now().minusWeeks(1),
            behandletDato = LocalDate.now(),
            perioder =
                listOf(
                    SykmeldingPeriode(
                        fom = LocalDate.now().minusWeeks(1),
                        tom = LocalDate.now().minusDays(3),
                        type = SykmeldingType.HUNDREPROSENT,
                    ),
                    SykmeldingPeriode(
                        fom = LocalDate.now().minusDays(2),
                        tom = LocalDate.now().plusDays(3),
                        type = SykmeldingType.GRADERT_50,
                    ),
                ),
            diagnosekode = "D71",
            diagnosekodesystem = "icpc2",
            utenOcr = false,
        )

    @BeforeEach
    fun before() = testApplication {
        setupTestApplication {
            dependencies { modules(module { single { papirsykmeldingService } }) }
        }
    }

    @AfterEach fun cleanup() = stopKoin()

    @Test
    internal fun `tilSkanningmetadata oppretter riktig skanningmetadata`() {
        coEvery { dokarkivClient.opprettJournalpost(any()) } returns "1"
        val skanningmetadata = papirsykmeldingService.tilSkanningmetadata(papirsykmeldingRequest)

        skanningmetadata.sykemeldinger.pasient.fnr shouldBeEqualTo fnr
        skanningmetadata.sykemeldinger.syketilfelleStartDato shouldBeEqualTo
            LocalDate.now().minusWeeks(1)
        skanningmetadata.sykemeldinger.behandler.hpr shouldBeEqualTo BigInteger.valueOf(7125186)
        skanningmetadata.sykemeldinger.medisinskVurdering.hovedDiagnose.size shouldBeEqualTo 1
        skanningmetadata.sykemeldinger.medisinskVurdering.hovedDiagnose[0]
            .diagnosekode shouldBeEqualTo "D71"
        skanningmetadata.sykemeldinger.medisinskVurdering.hovedDiagnose[0]
            .diagnosekodeSystem shouldBeEqualTo "icpc2"
        skanningmetadata.sykemeldinger.aktivitet.aktivitetIkkeMulig.periodeFOMDato shouldBeEqualTo
            LocalDate.now().minusWeeks(1)
        skanningmetadata.sykemeldinger.aktivitet.aktivitetIkkeMulig.periodeTOMDato shouldBeEqualTo
            LocalDate.now().minusDays(3)
        skanningmetadata.sykemeldinger.aktivitet.gradertSykmelding.periodeFOMDato shouldBeEqualTo
            LocalDate.now().minusDays(2)
        skanningmetadata.sykemeldinger.aktivitet.gradertSykmelding.periodeTOMDato shouldBeEqualTo
            LocalDate.now().plusDays(3)
        skanningmetadata.sykemeldinger.aktivitet.gradertSykmelding.sykmeldingsgrad shouldBeEqualTo
            "50"
        skanningmetadata.sykemeldinger.aktivitet.avventendeSykmelding shouldBeEqualTo null
        skanningmetadata.sykemeldinger.aktivitet.behandlingsdager shouldBeEqualTo null
        skanningmetadata.sykemeldinger.aktivitet.reisetilskudd shouldBeEqualTo null
        skanningmetadata.sykemeldinger.kontaktMedPasient.behandletDato shouldBeEqualTo
            LocalDate.now()
    }

    @Test
    internal fun `opprettPapirsykmelding oppretter journalpost med ocr`() {
        coEvery { dokarkivClient.opprettJournalpost(any()) } returns "1"
        runBlocking {
            papirsykmeldingService.opprettPapirsykmelding(papirsykmeldingRequest)

            coVerify {
                dokarkivClient.opprettJournalpost(
                    match { it.dokumenter[0].dokumentvarianter.size == 3 },
                )
            }
        }
    }

    @Test
    internal fun `opprettPapirsykmelding oppretter journalpost uten ocr hvis utenOcr er true`() {
        coEvery { dokarkivClient.opprettJournalpost(any()) } returns "1"
        runBlocking {
            papirsykmeldingService.opprettPapirsykmelding(
                papirsykmeldingRequest.copy(utenOcr = true),
            )

            coVerify {
                dokarkivClient.opprettJournalpost(
                    match { it.dokumenter[0].dokumentvarianter.size == 2 },
                )
            }
        }
    }

    @Test
    internal fun `opprettUtenlandskPapirsykmelding oppretter journalpost med riktig brevkode`() {
        coEvery { dokarkivClient.opprettJournalpost(any()) } returns "1"
        runBlocking {
            papirsykmeldingService.opprettUtenlandskPapirsykmelding(fnr)

            coVerify {
                dokarkivClient.opprettJournalpost(
                    match {
                        it.dokumenter[0].dokumentvarianter.size == 2 &&
                            it.dokumenter[0].brevkode == "NAV 08-07.04 U"
                    },
                )
            }
        }
    }

    @Test
    internal fun `opprettPapirsykmelding med fnr = null`() {
        coEvery { dokarkivClient.opprettJournalpost(any()) } returns "1"
        runBlocking {
            papirsykmeldingService.opprettPapirsykmelding(
                papirsykmeldingRequest.copy(fnr = null),
            )

            coVerify { dokarkivClient.opprettJournalpost(match { it.bruker?.id == null }) }
        }
    }

    @Test
    internal fun `opprettUtenlandskPapirsykmelding med fnr = null`() {
        coEvery { dokarkivClient.opprettJournalpost(any()) } returns "1"
        runBlocking {
            papirsykmeldingService.opprettUtenlandskPapirsykmelding(
                fnr = null,
            )
            coVerify { dokarkivClient.opprettJournalpost(match { it.bruker?.id == null }) }
        }
    }
}
