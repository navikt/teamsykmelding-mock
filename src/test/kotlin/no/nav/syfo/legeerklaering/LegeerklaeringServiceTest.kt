package no.nav.syfo.legeerklaering

import io.ktor.server.testing.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.helse.eiFellesformat.XMLMottakenhetBlokk
import no.nav.helse.legeerklaering.Legeerklaring
import no.nav.helse.msgHead.XMLMsgHead
import no.nav.syfo.legeerklaering.model.LegeerklaeringRequest
import no.nav.syfo.mq.MqClient
import no.nav.syfo.pdl.model.Navn
import no.nav.syfo.pdl.model.PdlPerson
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.utils.get
import no.nav.syfo.utils.setupTestApplication
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.koin.dsl.module

internal class LegeerklaeringServiceTest {
    private val pdlPersonService: PdlPersonService = mockk<PdlPersonService>()
    private val connection = mockk<MqClient>()
    private val legeerklaeringService = LegeerklaeringService(pdlPersonService, connection, "pale2")
    private val fnr = "12345678910"
    private val legeFnr = "10987654321"

    @BeforeEach
    fun before() = testApplication {
        setupTestApplication {
            dependencies { modules(module { single { legeerklaeringService } }) }
        }
        clearMocks(connection)
        coEvery { pdlPersonService.getPersoner(any()) } returns
            mapOf(
                fnr to PdlPerson(Navn("Syk", null, "Sykestad")),
                legeFnr to PdlPerson(Navn("Doktor", null, "Dyregod")),
            )
    }

    @AfterEach fun cleanup() = stopKoin()

    @Test
    internal fun `Oppretter korrekt legeerklaeringXml`() {
        val legeerklaeringRequest =
            LegeerklaeringRequest(
                fnr = fnr,
                fnrLege = legeFnr,
                diagnosekode = "M674",
                diagnosekodesystem = "icd10",
                statusPresens = null,
                vedlegg = false,
                vedleggMedVirus = false,
            )
        val mottakId = "mottakId"

        runBlocking {
            val legeerklaeringXml =
                legeerklaeringService.tilLegeerklaeringXml(legeerklaeringRequest, mottakId)

            val legeerklaering =
                legeerklaeringXml.get<XMLMsgHead>().document[0].refDoc.content.any[0]
                    as Legeerklaring
            legeerklaering.diagnoseArbeidsuforhet.statusPresens shouldBeEqualTo null
            legeerklaering.diagnoseArbeidsuforhet.diagnoseKodesystem.enkeltdiagnose
                .first()
                .kodeverdi shouldBeEqualTo "M674"
            legeerklaering.pasientopplysninger.pasient.fodselsnummer shouldBeEqualTo fnr

            legeerklaeringXml.get<XMLMsgHead>().msgInfo.msgId shouldNotBeEqualTo null
            legeerklaeringXml.get<XMLMottakenhetBlokk>().ediLoggId shouldBeEqualTo mottakId
            legeerklaeringXml.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur shouldBeEqualTo
                legeFnr
            legeerklaeringXml.get<XMLMsgHead>().document.size shouldBeEqualTo 1
            legeerklaeringXml
                .get<XMLMsgHead>()
                .msgInfo
                .sender
                .organisation
                .healthcareProfessional
                .ident
                .first()
                .id shouldBeEqualTo legeFnr
        }
    }

    @Test
    internal fun `Oppretter korrekt legeerklaeringXml med vedlegg`() {
        val legeerklaeringRequest =
            LegeerklaeringRequest(
                fnr = fnr,
                fnrLege = legeFnr,
                diagnosekode = "M674",
                diagnosekodesystem = "icd10",
                statusPresens = "status",
                vedlegg = true,
                vedleggMedVirus = false,
            )
        val mottakId = "mottakId"
        runBlocking {
            val legeerklaeringXml =
                legeerklaeringService.tilLegeerklaeringXml(legeerklaeringRequest, mottakId)

            val legeerklaering =
                legeerklaeringXml.get<XMLMsgHead>().document[0].refDoc.content.any[0]
                    as Legeerklaring
            legeerklaering.diagnoseArbeidsuforhet.statusPresens shouldBeEqualTo "status"
            legeerklaering.diagnoseArbeidsuforhet.diagnoseKodesystem.enkeltdiagnose
                .first()
                .kodeverdi shouldBeEqualTo "M674"
            legeerklaering.pasientopplysninger.pasient.fodselsnummer shouldBeEqualTo fnr

            legeerklaeringXml.get<XMLMsgHead>().msgInfo.msgId shouldNotBeEqualTo null
            legeerklaeringXml.get<XMLMottakenhetBlokk>().ediLoggId shouldBeEqualTo mottakId
            legeerklaeringXml.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur shouldBeEqualTo
                legeFnr
            legeerklaeringXml.get<XMLMsgHead>().document.size shouldBeEqualTo 3
            legeerklaeringXml
                .get<XMLMsgHead>()
                .msgInfo
                .sender
                .organisation
                .healthcareProfessional
                .ident
                .first()
                .id shouldBeEqualTo legeFnr
        }
    }
}
