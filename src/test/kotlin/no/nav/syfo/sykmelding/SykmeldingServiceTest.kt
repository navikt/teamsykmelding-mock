package no.nav.syfo.sykmelding

import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.mockk
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
import java.time.LocalDate
import javax.jms.Connection

class SykmeldingServiceTest : FunSpec({
    val pdlPersonService = mockk<PdlPersonService>()
    val syfosmreglerClient = mockk<SyfosmreglerClient>()
    val connection = mockk<Connection>()
    val sykmeldingService = SykmeldingService(pdlPersonService, connection, "syfosmmottak", syfosmreglerClient)
    val fnr = "12345678910"
    val legeFnr = "10987654321"

    beforeTest {
        coEvery { pdlPersonService.getPersoner(any()) } returns mapOf(
            fnr to PdlPerson(Navn("Syk", null, "Sykestad")),
            legeFnr to PdlPerson(Navn("Doktor", null, "Dyregod"))
        )
    }

    context("TilSykmeldingXml") {
        test("Oppretter korrekt sykmeldingXml") {
            val sykmeldingRequest = SykmeldingRequest(
                fnr = fnr,
                fnrLege = legeFnr,
                herId = null,
                hprNummer = null,
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                diagnosekode = "M674",
                diagnosekodesystem = "icd10",
                annenFraverGrunn = null,
                perioder = listOf(
                    SykmeldingPeriode(
                        fom = LocalDate.now().plusDays(1),
                        tom = LocalDate.now().plusWeeks(1),
                        type = SykmeldingType.HUNDREPROSENT
                    )
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
                yrkesskade = false
            )
            val mottakId = "mottakId"

            val sykmeldingXml = sykmeldingService.tilSykmeldingXml(sykmeldingRequest, mottakId)

            sykmeldingXml.get<XMLMsgHead>().msgInfo.msgId shouldNotBeEqualTo null
            sykmeldingXml.get<XMLMsgHead>().msgInfo.receiver.organisation.ident[0].id shouldBeEqualTo "1234556"
            sykmeldingXml.get<XMLMsgHead>().msgInfo.patient.ident[0].id shouldBeEqualTo fnr
            sykmeldingXml.get<XMLMottakenhetBlokk>().ediLoggId shouldBeEqualTo mottakId
            sykmeldingXml.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur shouldBeEqualTo legeFnr
            sykmeldingXml.get<XMLMsgHead>().document.size shouldBeEqualTo 1
        }
        test("Oppretter korrekt sykmeldingXml med her-id og vedlegg") {
            val sykmeldingRequest = SykmeldingRequest(
                fnr = fnr,
                fnrLege = legeFnr,
                herId = "herId",
                hprNummer = null,
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                diagnosekode = "M674",
                diagnosekodesystem = "icd10",
                annenFraverGrunn = null,
                perioder = listOf(
                    SykmeldingPeriode(
                        fom = LocalDate.now().plusDays(1),
                        tom = LocalDate.now().plusWeeks(1),
                        type = SykmeldingType.HUNDREPROSENT
                    )
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
                yrkesskade = false
            )
            val mottakId = "mottakId"

            val sykmeldingXml = sykmeldingService.tilSykmeldingXml(sykmeldingRequest, mottakId)

            sykmeldingXml.get<XMLMsgHead>().msgInfo.msgId shouldNotBeEqualTo null
            sykmeldingXml.get<XMLMsgHead>().msgInfo.receiver.organisation.ident[0].id shouldBeEqualTo "herId"
            sykmeldingXml.get<XMLMsgHead>().msgInfo.patient.ident[0].id shouldBeEqualTo fnr
            sykmeldingXml.get<XMLMottakenhetBlokk>().ediLoggId shouldBeEqualTo mottakId
            sykmeldingXml.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur shouldBeEqualTo legeFnr
            sykmeldingXml.get<XMLMsgHead>().document.size shouldBeEqualTo 3
        }
        test("Oppretter korrekt virksomhetsykmeldingXml") {
            val sykmeldingRequest = SykmeldingRequest(
                fnr = fnr,
                fnrLege = legeFnr,
                herId = null,
                hprNummer = "hpr",
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                diagnosekode = "M674",
                diagnosekodesystem = "icd10",
                annenFraverGrunn = null,
                perioder = listOf(
                    SykmeldingPeriode(
                        fom = LocalDate.now().plusDays(1),
                        tom = LocalDate.now().plusWeeks(1),
                        type = SykmeldingType.HUNDREPROSENT
                    )
                ),
                behandletDato = LocalDate.now(),
                kontaktDato = null,
                begrunnIkkeKontakt = null,
                vedlegg = false,
                virksomhetsykmelding = true,
                utdypendeOpplysninger = UtdypendeOpplysninger.UKE_39,
                regelsettVersjon = "2",
                meldingTilArbeidsgiver = null,
                bidiagnoser = listOf(
                    Diagnoser(
                        code = "M674",
                        system = "icd10",
                        text = "Ein viktig diagnose"
                    )
                ),
                arbeidsgiverNavn = null,
                vedleggMedVirus = false,
                yrkesskade = false
            )
            val mottakId = "mottakId"

            val sykmeldingXml = sykmeldingService.tilSykmeldingXml(sykmeldingRequest, mottakId)

            sykmeldingXml.get<XMLMsgHead>().msgInfo.msgId shouldNotBeEqualTo null
            sykmeldingXml.get<XMLMsgHead>().msgInfo.sender.organisation.healthcareProfessional?.ident?.find {
                it.typeId.v == "HPR"
            }?.id shouldBeEqualTo "hpr"
            sykmeldingXml.get<XMLMsgHead>().msgInfo.patient.ident[0].id shouldBeEqualTo fnr
            sykmeldingXml.get<XMLMottakenhetBlokk>().ediLoggId shouldBeEqualTo mottakId
            sykmeldingXml.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur shouldBeEqualTo null
            sykmeldingXml.get<XMLMottakenhetBlokk>().ebService shouldBeEqualTo "SykmeldingVirksomhet"
            sykmeldingXml.get<XMLMsgHead>().document.size shouldBeEqualTo 1
        }
    }
})
