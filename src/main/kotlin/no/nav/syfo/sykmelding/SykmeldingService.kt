package no.nav.syfo.sykmelding

import no.nav.helse.eiFellesformat.XMLEIFellesformat
import no.nav.helse.eiFellesformat.XMLMottakenhetBlokk
import no.nav.helse.msgHead.XMLMsgHead
import no.nav.helse.sm2013.HelseOpplysningerArbeidsuforhet
import no.nav.syfo.log
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.mq.MqProducer
import no.nav.syfo.mq.producerForQueue
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.sykmelding.client.SyfosmreglerClient
import no.nav.syfo.sykmelding.model.SykmeldingRequest
import no.nav.syfo.util.fellesformatUnmarshaller
import no.nav.syfo.util.get
import no.nav.syfo.util.marshallFellesformat
import java.io.StringReader
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import java.util.UUID
import javax.jms.Connection
import javax.jms.Session

class SykmeldingService(
    private val pdlPersonService: PdlPersonService,
    private val connection: Connection,
    private val sykmeldingQueue: String,
    private val syfosmreglerClient: SyfosmreglerClient
) {
    suspend fun opprettSykmelding(sykmeldingRequest: SykmeldingRequest): String {
        val mottakId = UUID.randomUUID().toString()
        val sykmelding = tilSykmeldingXml(sykmeldingRequest, mottakId)
        val sykmeldingXml = marshallFellesformat(sykmelding)

        val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        val messageProducer = session.producerForQueue(sykmeldingQueue)
        val syfosmmottakMqProducer = MqProducer(session, messageProducer)

        syfosmmottakMqProducer.send(sykmeldingXml)

        return mottakId
    }

    suspend fun sjekkRegler(sykmeldingRequest: SykmeldingRequest): ValidationResult {
        val sykmeldingXml = tilSykmeldingXml(sykmeldingRequest, UUID.randomUUID().toString())
        val helseOpplysningerArbeidsuforhet = sykmeldingXml.get<XMLMsgHead>().document[0].refDoc.content.any[0] as HelseOpplysningerArbeidsuforhet

        val sykmelding = helseOpplysningerArbeidsuforhet.toSykmelding(
            sykmeldingId = UUID.randomUUID().toString(),
            pasientAktoerId = "",
            legeAktoerId = "",
            msgId = UUID.randomUUID().toString(),
            signaturDato = LocalDateTime.now(),
            behandlerFnr = sykmeldingRequest.fnrLege,
            behandlerHprNr = null
        )

        val receivedSykmelding = ReceivedSykmelding(
            sykmelding = sykmelding,
            personNrPasient = sykmeldingRequest.fnr,
            tlfPasient = extractTlfFromKontaktInfo(helseOpplysningerArbeidsuforhet.pasient.kontaktInfo),
            personNrLege = sykmeldingRequest.fnrLege,
            navLogId = UUID.randomUUID().toString(),
            msgId = UUID.randomUUID().toString(),
            legeHprNr = sykmeldingRequest.hprNummer,
            legeHelsepersonellkategori = null,
            legekontorOrgNr = null,
            legekontorOrgName = "",
            legekontorHerId = "",
            legekontorReshId = "",
            mottattDato = sykmeldingXml.get<XMLMottakenhetBlokk>().mottattDatotid.toGregorianCalendar().toZonedDateTime()
                .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
            rulesetVersion = sykmeldingRequest.regelsettVersjon,
            fellesformat = marshallFellesformat(sykmeldingXml),
            tssid = "",
            merknader = null,
            partnerreferanse = "",
            vedlegg = null,
            utenlandskSykmelding = null
        )

        return syfosmreglerClient.sjekkRegler(receivedSykmelding)
    }

    suspend fun tilSykmeldingXml(sykmeldingRequest: SykmeldingRequest, mottakId: String): XMLEIFellesformat {
        val sykmeldingXml = if (sykmeldingRequest.vedlegg) {
            SykmeldingService::class.java.getResource("/sykmelding/sykmelding_med_vedlegg.xml")!!.readText(charset = Charsets.ISO_8859_1)
        } else if (sykmeldingRequest.vedleggMedVirus) {
            SykmeldingService::class.java.getResource("/sykmelding/sykmelding_med_vedlegg_virus.xml")!!.readText(charset = Charsets.ISO_8859_1)
        } else if (sykmeldingRequest.virksomhetsykmelding) {
            SykmeldingService::class.java.getResource("/sykmelding/virksomhetsykmelding.xml")!!.readText(charset = Charsets.ISO_8859_1)
        } else if (sykmeldingRequest.regelsettVersjon == "3") {
            SykmeldingService::class.java.getResource("/sykmelding/sykmelding_regelsett3.xml")!!.readText(charset = Charsets.ISO_8859_1)
        } else {
            SykmeldingService::class.java.getResource("/sykmelding/sykmelding.xml")!!.readText(charset = Charsets.ISO_8859_1)
        }
        val fellesformat = fellesformatUnmarshaller.unmarshal(StringReader(sykmeldingXml)) as XMLEIFellesformat

        val personer = pdlPersonService.getPersoner(listOf(sykmeldingRequest.fnr, sykmeldingRequest.fnrLege))
        val sykmeldt = personer[sykmeldingRequest.fnr]
        val lege = personer[sykmeldingRequest.fnrLege]

        if (sykmeldt == null || lege == null) {
            log.error("Fant ikke sykmeldt eller lege i PDL")
            throw RuntimeException("Fant ikke sykmeldt eller lege i PDL")
        }

        val sykmelding = lagHelseopplysninger(
            sykmeldingRequest = sykmeldingRequest,
            sykmeldt = sykmeldt,
            lege = lege
        )

        val pasient = fellesformat.get<XMLMsgHead>().msgInfo.patient

        fellesformat.get<XMLMsgHead>().msgInfo.genDate = LocalDateTime.now().format(ISO_LOCAL_DATE_TIME)
        pasient.ident.forEach { ident -> ident.id = sykmeldingRequest.fnr }
        fellesformat.get<XMLMsgHead>().document[0].refDoc.content.any[0] = sykmelding
        fellesformat.get<XMLMsgHead>().msgInfo.msgId = UUID.randomUUID().toString()
        sykmeldingRequest.herId?.let {
            fellesformat.get<XMLMsgHead>().msgInfo.receiver.organisation.ident[0] = xmlIdentHerid(it)
        }
        sykmeldingRequest.hprNummer?.let {
            fellesformat.get<XMLMsgHead>().msgInfo.sender.organisation.healthcareProfessional.ident.add(xmlIdentHPR(sykmeldingRequest.hprNummer))
        }
        fellesformat.get<XMLMottakenhetBlokk>().ediLoggId = mottakId
        fellesformat.get<XMLMottakenhetBlokk>().mottattDatotid = convertToXmlGregorianCalendar(sykmeldingRequest.behandletDato)

        if (!sykmeldingRequest.virksomhetsykmelding) {
            fellesformat.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur = sykmeldingRequest.fnrLege
        }

        return fellesformat
    }
}
