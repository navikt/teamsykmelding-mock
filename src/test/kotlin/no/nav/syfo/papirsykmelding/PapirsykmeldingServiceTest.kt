package no.nav.syfo.papirsykmelding

import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.math.BigInteger
import java.time.LocalDate
import no.nav.syfo.model.SykmeldingPeriode
import no.nav.syfo.model.SykmeldingType
import no.nav.syfo.papirsykmelding.client.DokarkivClient
import no.nav.syfo.papirsykmelding.client.NorskHelsenettClient
import no.nav.syfo.papirsykmelding.client.SyfosmpapirreglerClient
import no.nav.syfo.papirsykmelding.model.PapirsykmeldingRequest
import org.amshove.kluent.shouldBeEqualTo

class PapirsykmeldingServiceTest :
    FunSpec({
        val dokarkivClient = mockk<DokarkivClient>()
        val syfosmpapirreglerClient = mockk<SyfosmpapirreglerClient>()
        val norskHelsenettClient = mockk<NorskHelsenettClient>()
        val papirsykmeldingService =
            PapirsykmeldingService(dokarkivClient, syfosmpapirreglerClient, norskHelsenettClient)
        val fnr = "1234567890"

        coEvery { dokarkivClient.opprettJournalpost(any()) } returns "1"

        context("PapirsykmeldingService") {
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

            test("tilSkanningmetadata oppretter riktig skanningmetadata") {
                val skanningmetadata =
                    papirsykmeldingService.tilSkanningmetadata(papirsykmeldingRequest)

                skanningmetadata.sykemeldinger.pasient.fnr shouldBeEqualTo fnr
                skanningmetadata.sykemeldinger.syketilfelleStartDato shouldBeEqualTo
                    LocalDate.now().minusWeeks(1)
                skanningmetadata.sykemeldinger.behandler.hpr shouldBeEqualTo
                    BigInteger.valueOf(7125186)
                skanningmetadata.sykemeldinger.medisinskVurdering.hovedDiagnose.size shouldBeEqualTo
                    1
                skanningmetadata.sykemeldinger.medisinskVurdering.hovedDiagnose[0]
                    .diagnosekode shouldBeEqualTo "D71"
                skanningmetadata.sykemeldinger.medisinskVurdering.hovedDiagnose[0]
                    .diagnosekodeSystem shouldBeEqualTo "icpc2"
                skanningmetadata.sykemeldinger.aktivitet.aktivitetIkkeMulig
                    .periodeFOMDato shouldBeEqualTo LocalDate.now().minusWeeks(1)
                skanningmetadata.sykemeldinger.aktivitet.aktivitetIkkeMulig
                    .periodeTOMDato shouldBeEqualTo LocalDate.now().minusDays(3)
                skanningmetadata.sykemeldinger.aktivitet.gradertSykmelding
                    .periodeFOMDato shouldBeEqualTo LocalDate.now().minusDays(2)
                skanningmetadata.sykemeldinger.aktivitet.gradertSykmelding
                    .periodeTOMDato shouldBeEqualTo LocalDate.now().plusDays(3)
                skanningmetadata.sykemeldinger.aktivitet.gradertSykmelding
                    .sykmeldingsgrad shouldBeEqualTo "50"
                skanningmetadata.sykemeldinger.aktivitet.avventendeSykmelding shouldBeEqualTo null
                skanningmetadata.sykemeldinger.aktivitet.behandlingsdager shouldBeEqualTo null
                skanningmetadata.sykemeldinger.aktivitet.reisetilskudd shouldBeEqualTo null
                skanningmetadata.sykemeldinger.kontaktMedPasient.behandletDato shouldBeEqualTo
                    LocalDate.now()
            }
            test("opprettPapirsykmelding oppretter journalpost med ocr") {
                papirsykmeldingService.opprettPapirsykmelding(papirsykmeldingRequest)

                coVerify {
                    dokarkivClient.opprettJournalpost(
                        match { it.dokumenter[0].dokumentvarianter.size == 3 }
                    )
                }
            }
            test("opprettPapirsykmelding oppretter journalpost uten ocr hvis utenOcr er true") {
                papirsykmeldingService.opprettPapirsykmelding(
                    papirsykmeldingRequest.copy(utenOcr = true)
                )

                coVerify {
                    dokarkivClient.opprettJournalpost(
                        match { it.dokumenter[0].dokumentvarianter.size == 2 }
                    )
                }
            }
            test("opprettUtenlandskPapirsykmelding oppretter journalpost med riktig brevkode") {
                papirsykmeldingService.opprettUtenlandskPapirsykmelding(fnr)

                coVerify {
                    dokarkivClient.opprettJournalpost(
                        match {
                            it.dokumenter[0].dokumentvarianter.size == 2 &&
                                it.dokumenter[0].brevkode == "NAV 08-07.04 U"
                        }
                    )
                }
            }
        }
    })
