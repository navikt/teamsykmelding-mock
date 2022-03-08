package no.nav.syfo.legeerklaering

import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.helse.eiFellesformat.XMLMottakenhetBlokk
import no.nav.helse.legeerklaering.Legeerklaring
import no.nav.helse.msgHead.XMLMsgHead
import no.nav.syfo.legeerklaering.model.LegeerklaeringRequest
import no.nav.syfo.pdl.model.Navn
import no.nav.syfo.pdl.model.PdlPerson
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.util.get
import org.amshove.kluent.shouldBeEqualTo
import javax.jms.Connection

class LegeerklaeringServiceTest : FunSpec({
    val pdlPersonService: PdlPersonService = mockk<PdlPersonService>()
    val connection = mockk<Connection>()
    val legeerklaeringService = LegeerklaeringService(pdlPersonService, connection, "pale2")
    val fnr = "12345678910"
    val legeFnr = "10987654321"

    beforeTest {
        coEvery { pdlPersonService.getPersoner(any()) } returns mapOf(
            fnr to PdlPerson(Navn("Syk", null, "Sykestad")),
            legeFnr to PdlPerson(Navn("Doktor", null, "Dyregod"))
        )
    }

    context("TilLegeerklaeringXml") {
        test("Oppretter korrekt legeerklaeringXml") {
            val legeerklaeringRequest = LegeerklaeringRequest(
                fnr = fnr,
                mottakId = "mottakId",
                msgId = "msgId",
                fnrLege = legeFnr,
                diagnosekode = "M674",
                statusPresens = null,
                vedlegg = false
            )

            val legeerklaeringXml = legeerklaeringService.tilLegeerklaeringXml(legeerklaeringRequest)

            val legeerklaering = legeerklaeringXml.get<XMLMsgHead>().document[0].refDoc.content.any[0] as Legeerklaring
            legeerklaering.diagnoseArbeidsuforhet.statusPresens shouldBeEqualTo null
            legeerklaering.diagnoseArbeidsuforhet.diagnoseKodesystem.enkeltdiagnose.first().kodeverdi shouldBeEqualTo "M674"
            legeerklaering.pasientopplysninger.pasient.fodselsnummer shouldBeEqualTo fnr

            legeerklaeringXml.get<XMLMsgHead>().msgInfo.msgId shouldBeEqualTo "msgId"
            legeerklaeringXml.get<XMLMottakenhetBlokk>().ediLoggId shouldBeEqualTo "mottakId"
            legeerklaeringXml.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur shouldBeEqualTo legeFnr
            legeerklaeringXml.get<XMLMsgHead>().document.size shouldBeEqualTo 1
            legeerklaeringXml.get<XMLMsgHead>().msgInfo.sender.organisation.healthcareProfessional.ident.first().id shouldBeEqualTo legeFnr
        }
        test("Oppretter korrekt legeerklaeringXml med vedlegg") {
            val legeerklaeringRequest = LegeerklaeringRequest(
                fnr = fnr,
                mottakId = "mottakId",
                msgId = "msgId",
                fnrLege = legeFnr,
                diagnosekode = "M674",
                statusPresens = "status",
                vedlegg = true
            )

            val legeerklaeringXml = legeerklaeringService.tilLegeerklaeringXml(legeerklaeringRequest)

            val legeerklaering = legeerklaeringXml.get<XMLMsgHead>().document[0].refDoc.content.any[0] as Legeerklaring
            legeerklaering.diagnoseArbeidsuforhet.statusPresens shouldBeEqualTo "status"
            legeerklaering.diagnoseArbeidsuforhet.diagnoseKodesystem.enkeltdiagnose.first().kodeverdi shouldBeEqualTo "M674"
            legeerklaering.pasientopplysninger.pasient.fodselsnummer shouldBeEqualTo fnr

            legeerklaeringXml.get<XMLMsgHead>().msgInfo.msgId shouldBeEqualTo "msgId"
            legeerklaeringXml.get<XMLMottakenhetBlokk>().ediLoggId shouldBeEqualTo "mottakId"
            legeerklaeringXml.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur shouldBeEqualTo legeFnr
            legeerklaeringXml.get<XMLMsgHead>().document.size shouldBeEqualTo 3
            legeerklaeringXml.get<XMLMsgHead>().msgInfo.sender.organisation.healthcareProfessional.ident.first().id shouldBeEqualTo legeFnr
        }
    }
})
