package no.nav.syfo.sykmelding

import no.nav.helse.eiFellesformat.XMLEIFellesformat
import no.nav.helse.eiFellesformat.XMLMottakenhetBlokk
import no.nav.helse.msgHead.XMLMsgHead
import no.nav.syfo.log
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.sykmelding.model.SykmeldingRequest
import no.nav.syfo.sykmelding.mq.SyfosmmottakMqProducer
import no.nav.syfo.util.fellesformatUnmarshaller
import no.nav.syfo.util.get
import no.nav.syfo.util.marshallFellesformat
import java.io.StringReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

class SykmeldingService(
    private val pdlPersonService: PdlPersonService,
    private val syfosmmottakMqProducer: SyfosmmottakMqProducer
) {
    suspend fun opprettSykmelding(sykmeldingRequest: SykmeldingRequest) {
        val sykmelding = tilSykmeldingXml(sykmeldingRequest)
        val sykmeldingXml = marshallFellesformat(sykmelding)
        syfosmmottakMqProducer.send(sykmeldingXml)
    }

    suspend fun tilSykmeldingXml(sykmeldingRequest: SykmeldingRequest): XMLEIFellesformat {
        val sykmeldingXml = if (sykmeldingRequest.vedlegg) {
            SykmeldingService::class.java.getResource("/sykmelding/sykmelding_med_vedlegg.xml").readText(charset = Charsets.ISO_8859_1)
        } else {
            SykmeldingService::class.java.getResource("/sykmelding/sykmelding.xml").readText(charset = Charsets.ISO_8859_1)
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
        fellesformat.get<XMLMsgHead>().msgInfo.msgId = sykmeldingRequest.msgId
        sykmeldingRequest.herId?.let {
            fellesformat.get<XMLMsgHead>().msgInfo.receiver.organisation.ident[0] = hentXmlIdentHerid(it)
        }
        fellesformat.get<XMLMottakenhetBlokk>().ediLoggId = sykmeldingRequest.mottakId
        fellesformat.get<XMLMottakenhetBlokk>().mottattDatotid = convertToXmlGregorianCalendar(sykmeldingRequest.behandletDato)
        fellesformat.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur = sykmeldingRequest.fnrLege

        return fellesformat
    }
}
