package no.nav.syfo.sykmelding

import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDate
import javax.jms.Connection
import kotlinx.coroutines.runBlocking
import no.nav.helse.eiFellesformat.XMLMottakenhetBlokk
import no.nav.helse.msgHead.XMLMsgHead
import no.nav.syfo.model.SykmeldingPeriode
import no.nav.syfo.model.SykmeldingType
import no.nav.syfo.pdl.model.Navn
import no.nav.syfo.pdl.model.PdlPerson
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.sykmelding.client.SyfosmreglerClient
import no.nav.syfo.sykmelding.model.Diagnoser
import no.nav.syfo.sykmelding.model.SykmeldingRequest
import no.nav.syfo.sykmelding.model.UtdypendeOpplysninger
import no.nav.syfo.util.get
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SykmeldingServiceTest {
    private val pdlPersonService = mockk<PdlPersonService>()
    private val syfosmreglerClient = mockk<SyfosmreglerClient>()
    private val connection = mockk<Connection>()
    private val sykmeldingService =
        SykmeldingService(pdlPersonService, connection, "syfosmmottak", syfosmreglerClient)
    private val fnr = "12345678910"
    private val legeFnr = "10987654321"

    @BeforeEach
    internal fun `Set up`() {
        coEvery { pdlPersonService.getPersoner(any()) } returns
            mapOf(
                fnr to PdlPerson(Navn("Syk", null, "Sykestad")),
                legeFnr to PdlPerson(Navn("Doktor", null, "Dyregod")),
            )
    }

    @Test
    internal fun `Oppretter korrekt sykmeldingXml`() {
        val sykmeldingRequest =
            SykmeldingRequest(
                fnr = fnr,
                fnrLege = legeFnr,
                herId = null,
                hprNummer = null,
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                diagnosekode = "M674",
                diagnosekodesystem = "icd10",
                annenFraverGrunn = null,
                beskrivBistandNav = null,
                perioder =
                    listOf(
                        SykmeldingPeriode(
                            fom = LocalDate.now().plusDays(1),
                            tom = LocalDate.now().plusWeeks(1),
                            type = SykmeldingType.HUNDREPROSENT,
                        ),
                    ),
                behandletDato = LocalDate.now(),
                kontaktDato = null,
                begrunnIkkeKontakt = null,
                vedlegg = false,
                virksomhetsykmelding = false,
                utdypendeOpplysninger = UtdypendeOpplysninger.UKE_39,
                regelsettVersjon = "2",
                meldingTilArbeidsgiver = null,
                bidiagnoser = null,
                arbeidsgiverNavn = null,
                vedleggMedVirus = false,
                yrkesskade = false,
            )
        val mottakId = "mottakId"
        runBlocking {
            val sykmeldingXml = sykmeldingService.tilSykmeldingXml(sykmeldingRequest, mottakId)

            sykmeldingXml.get<XMLMsgHead>().msgInfo.msgId shouldNotBeEqualTo null
            sykmeldingXml
                .get<XMLMsgHead>()
                .msgInfo
                .receiver
                .organisation
                .ident[0]
                .id shouldBeEqualTo "1234556"
            sykmeldingXml.get<XMLMsgHead>().msgInfo.patient.ident[0].id shouldBeEqualTo fnr
            sykmeldingXml.get<XMLMottakenhetBlokk>().ediLoggId shouldBeEqualTo mottakId
            sykmeldingXml.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur shouldBeEqualTo
                legeFnr
            sykmeldingXml.get<XMLMsgHead>().document.size shouldBeEqualTo 1
        }
    }

    @Test
    internal fun `Oppretter korrekt sykmeldingXml med her-id og vedlegg`() {
        val sykmeldingRequest =
            SykmeldingRequest(
                fnr = fnr,
                fnrLege = legeFnr,
                herId = "herId",
                hprNummer = null,
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                diagnosekode = "M674",
                diagnosekodesystem = "icd10",
                annenFraverGrunn = null,
                perioder =
                    listOf(
                        SykmeldingPeriode(
                            fom = LocalDate.now().plusDays(1),
                            tom = LocalDate.now().plusWeeks(1),
                            type = SykmeldingType.HUNDREPROSENT,
                        ),
                    ),
                behandletDato = LocalDate.now(),
                kontaktDato = null,
                begrunnIkkeKontakt = null,
                vedlegg = true,
                virksomhetsykmelding = false,
                utdypendeOpplysninger = UtdypendeOpplysninger.UKE_39,
                regelsettVersjon = "2",
                meldingTilArbeidsgiver = null,
                bidiagnoser = emptyList(),
                arbeidsgiverNavn = null,
                vedleggMedVirus = false,
                beskrivBistandNav = null,
                yrkesskade = false,
            )
        val mottakId = "mottakId"
        runBlocking {
            val sykmeldingXml = sykmeldingService.tilSykmeldingXml(sykmeldingRequest, mottakId)

            sykmeldingXml.get<XMLMsgHead>().msgInfo.msgId shouldNotBeEqualTo null
            sykmeldingXml
                .get<XMLMsgHead>()
                .msgInfo
                .receiver
                .organisation
                .ident[0]
                .id shouldBeEqualTo "herId"
            sykmeldingXml.get<XMLMsgHead>().msgInfo.patient.ident[0].id shouldBeEqualTo fnr
            sykmeldingXml.get<XMLMottakenhetBlokk>().ediLoggId shouldBeEqualTo mottakId
            sykmeldingXml.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur shouldBeEqualTo
                legeFnr
            sykmeldingXml.get<XMLMsgHead>().document.size shouldBeEqualTo 3
        }
    }

    @Test
    internal fun `Oppretter korrekt virksomhetsykmeldingXml`() {
        val sykmeldingRequest =
            SykmeldingRequest(
                fnr = fnr,
                fnrLege = legeFnr,
                herId = null,
                hprNummer = "hpr",
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                diagnosekode = "M674",
                diagnosekodesystem = "icd10",
                annenFraverGrunn = null,
                perioder =
                    listOf(
                        SykmeldingPeriode(
                            fom = LocalDate.now().plusDays(1),
                            tom = LocalDate.now().plusWeeks(1),
                            type = SykmeldingType.HUNDREPROSENT,
                        ),
                    ),
                behandletDato = LocalDate.now(),
                kontaktDato = null,
                begrunnIkkeKontakt = null,
                vedlegg = false,
                virksomhetsykmelding = true,
                utdypendeOpplysninger = UtdypendeOpplysninger.UKE_39,
                regelsettVersjon = "2",
                meldingTilArbeidsgiver = null,
                bidiagnoser =
                    listOf(
                        Diagnoser(
                            code = "M674",
                            system = "icd10",
                            text = "Ein viktig diagnose",
                        ),
                    ),
                arbeidsgiverNavn = null,
                vedleggMedVirus = false,
                beskrivBistandNav = null,
                yrkesskade = false,
            )
        val mottakId = "mottakId"
        runBlocking {
            val sykmeldingXml = sykmeldingService.tilSykmeldingXml(sykmeldingRequest, mottakId)

            sykmeldingXml.get<XMLMsgHead>().msgInfo.msgId shouldNotBeEqualTo null
            sykmeldingXml
                .get<XMLMsgHead>()
                .msgInfo
                .sender
                .organisation
                .healthcareProfessional
                ?.ident
                ?.find { it.typeId.v == "HPR" }
                ?.id shouldBeEqualTo "hpr"
            sykmeldingXml.get<XMLMsgHead>().msgInfo.patient.ident[0].id shouldBeEqualTo fnr
            sykmeldingXml.get<XMLMottakenhetBlokk>().ediLoggId shouldBeEqualTo mottakId
            sykmeldingXml.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur shouldBeEqualTo null
            sykmeldingXml.get<XMLMottakenhetBlokk>().ebService shouldBeEqualTo
                "SykmeldingVirksomhet"
            sykmeldingXml.get<XMLMsgHead>().document.size shouldBeEqualTo 1
        }
    }
}
