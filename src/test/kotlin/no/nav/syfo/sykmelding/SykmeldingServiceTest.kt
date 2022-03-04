package no.nav.syfo.sykmelding

import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.helse.eiFellesformat.XMLMottakenhetBlokk
import no.nav.helse.msgHead.XMLMsgHead
import no.nav.syfo.pdl.model.Navn
import no.nav.syfo.pdl.model.PdlPerson
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.sykmelding.model.SykmeldingPeriode
import no.nav.syfo.sykmelding.model.SykmeldingRequest
import no.nav.syfo.sykmelding.model.SykmeldingType
import no.nav.syfo.util.get
import org.amshove.kluent.shouldBeEqualTo
import java.time.LocalDate
import javax.jms.Connection

class SykmeldingServiceTest : FunSpec({
    val pdlPersonService: PdlPersonService = mockk<PdlPersonService>()
    val connection = mockk<Connection>()
    val sykmeldingService = SykmeldingService(pdlPersonService, connection, "syfosmmottak")
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
                mottakId = "mottakId",
                fnrLege = legeFnr,
                msgId = "msgId",
                herId = null,
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                diagnosekode = "M674",
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
                vedlegg = false
            )

            val sykmeldingXml = sykmeldingService.tilSykmeldingXml(sykmeldingRequest)

            sykmeldingXml.get<XMLMsgHead>().msgInfo.msgId shouldBeEqualTo "msgId"
            sykmeldingXml.get<XMLMsgHead>().msgInfo.receiver.organisation.ident[0].id shouldBeEqualTo "1234556"
            sykmeldingXml.get<XMLMsgHead>().msgInfo.patient.ident[0].id shouldBeEqualTo fnr
            sykmeldingXml.get<XMLMottakenhetBlokk>().ediLoggId shouldBeEqualTo "mottakId"
            sykmeldingXml.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur shouldBeEqualTo legeFnr
            sykmeldingXml.get<XMLMsgHead>().document.size shouldBeEqualTo 1
        }
        test("Oppretter korrekt sykmeldingXml med her-id og vedlegg") {
            val sykmeldingRequest = SykmeldingRequest(
                fnr = fnr,
                mottakId = "mottakId",
                fnrLege = legeFnr,
                msgId = "msgId",
                herId = "herId",
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                diagnosekode = "M674",
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
                vedlegg = true
            )

            val sykmeldingXml = sykmeldingService.tilSykmeldingXml(sykmeldingRequest)

            sykmeldingXml.get<XMLMsgHead>().msgInfo.msgId shouldBeEqualTo "msgId"
            sykmeldingXml.get<XMLMsgHead>().msgInfo.receiver.organisation.ident[0].id shouldBeEqualTo "herId"
            sykmeldingXml.get<XMLMsgHead>().msgInfo.patient.ident[0].id shouldBeEqualTo fnr
            sykmeldingXml.get<XMLMottakenhetBlokk>().ediLoggId shouldBeEqualTo "mottakId"
            sykmeldingXml.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur shouldBeEqualTo legeFnr
            sykmeldingXml.get<XMLMsgHead>().document.size shouldBeEqualTo 3
        }
    }
})
