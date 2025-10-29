package no.nav.syfo.legeerklaering

import jakarta.jms.Session
import java.io.StringReader
import java.math.BigInteger
import java.time.LocalDate
import java.util.UUID
import no.nav.helse.eiFellesformat.XMLEIFellesformat
import no.nav.helse.eiFellesformat.XMLMottakenhetBlokk
import no.nav.helse.legeerklaering.Arbeidsforhold
import no.nav.helse.legeerklaering.DiagnoseArbeidsuforhet
import no.nav.helse.legeerklaering.DiagnoseKodesystem
import no.nav.helse.legeerklaering.Enkeltdiagnose
import no.nav.helse.legeerklaering.Legeerklaring
import no.nav.helse.legeerklaering.Pasient
import no.nav.helse.legeerklaering.Pasientopplysninger
import no.nav.helse.legeerklaering.TypeAdresse
import no.nav.helse.legeerklaering.TypeAdressetype
import no.nav.helse.legeerklaering.TypeNavn
import no.nav.helse.legeerklaering.Virksomhet
import no.nav.helse.legeerklaering.VurderingYrkesskade
import no.nav.helse.msgHead.XMLCV
import no.nav.helse.msgHead.XMLHealthcareProfessional
import no.nav.helse.msgHead.XMLIdent
import no.nav.helse.msgHead.XMLMsgHead
import no.nav.syfo.legeerklaering.model.LegeerklaeringRequest
import no.nav.syfo.mq.MqClient
import no.nav.syfo.mq.MqProducer
import no.nav.syfo.mq.producerForQueue
import no.nav.syfo.pdl.model.PdlPerson
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.sykmelding.convertToXmlGregorianCalendar
import no.nav.syfo.utils.get
import no.nav.syfo.utils.legeerklaeringUnmarshaller
import no.nav.syfo.utils.logger
import no.nav.syfo.utils.marshallLegeerklaering
import java.time.LocalDateTime

class LegeerklaeringService(
    private val pdlPersonService: PdlPersonService,
    private val mqClient: MqClient,
    private val legeerklaeringQueue: String,
) {
    suspend fun opprettLegeerklaering(legeerklaeringRequest: LegeerklaeringRequest): String {
        val connection = mqClient.connection
        val mottakId = UUID.randomUUID().toString()
        if (connection != null) {
            val legeerklaering = tilLegeerklaeringXml(legeerklaeringRequest, mottakId)
            val legeerklaeringXml = marshallLegeerklaering(legeerklaering)

            val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
            val messageProducer = session.producerForQueue(legeerklaeringQueue)
            val pale2MqProducer = MqProducer(session, messageProducer)

            pale2MqProducer.send(legeerklaeringXml)
        }
        return mottakId
    }

    suspend fun tilLegeerklaeringXml(
        legeerklaeringRequest: LegeerklaeringRequest,
        mottakId: String
    ): XMLEIFellesformat {
        val legeerklaeringXml =
            if (legeerklaeringRequest.vedlegg) {
                LegeerklaeringService::class
                    .java
                    .getResource("/legeerklaering/legeerklaering_med_vedlegg.xml")!!
                    .readText(charset = Charsets.ISO_8859_1)
            } else if (legeerklaeringRequest.vedleggMedVirus) {
                LegeerklaeringService::class
                    .java
                    .getResource("/legeerklaering/legeerklaering_med_vedlegg_virus.xml")!!
                    .readText(charset = Charsets.ISO_8859_1)
            } else {
                LegeerklaeringService::class
                    .java
                    .getResource("/legeerklaering/legeerklaering.xml")!!
                    .readText(charset = Charsets.ISO_8859_1)
            }

        val fellesformat =
            legeerklaeringUnmarshaller.unmarshal(StringReader(legeerklaeringXml))
                as XMLEIFellesformat
        fellesformat.get<XMLMsgHead>().msgInfo.genDate = LocalDateTime.now().toString()

        val personer =
            pdlPersonService.getPersoner(
                listOf(legeerklaeringRequest.fnr, legeerklaeringRequest.fnrLege),
            )
        val pasient = personer[legeerklaeringRequest.fnr]
        val lege = personer[legeerklaeringRequest.fnrLege]

        if (pasient == null || lege == null) {
            logger.error("Fant ikke pasient eller lege i PDL")
            throw RuntimeException("Fant ikke pasient eller lege i PDL")
        }

        val legeerklaering =
            fellesformat.get<XMLMsgHead>().document[0].refDoc.content.any[0] as Legeerklaring
        legeerklaering.arsakssammenhengLegeerklaring =
            "Vedkommende har vært syk lenge, duplikatbuster: ${UUID.randomUUID()}"
        legeerklaering.diagnoseArbeidsuforhet =
            diagnoseArbeidsuforhet(
                legeerklaeringRequest.diagnosekode,
                legeerklaeringRequest.statusPresens,
            )
        legeerklaering.pasientopplysninger = pasientopplysninger(legeerklaeringRequest.fnr, pasient)

        fellesformat.get<XMLMsgHead>().document[0].refDoc.content.any[0] = legeerklaering
        fellesformat.get<XMLMsgHead>().msgInfo.msgId = UUID.randomUUID().toString()
        fellesformat.get<XMLMsgHead>().msgInfo.sender.organisation.healthcareProfessional =
            opprettHealthcareProfessional(legeerklaeringRequest.fnrLege, lege)
        fellesformat.get<XMLMottakenhetBlokk>().ediLoggId = mottakId
        fellesformat.get<XMLMottakenhetBlokk>().mottattDatotid =
            convertToXmlGregorianCalendar(LocalDate.now())
        fellesformat.get<XMLMottakenhetBlokk>().avsenderFnrFraDigSignatur =
            legeerklaeringRequest.fnrLege
        return fellesformat
    }

    private fun diagnoseArbeidsuforhet(
        diagnosekode: String,
        statuspresens: String?
    ): DiagnoseArbeidsuforhet {
        return DiagnoseArbeidsuforhet().apply {
            diagnoseKodesystem =
                DiagnoseKodesystem().apply {
                    kodesystem = BigInteger.valueOf(1)
                    enkeltdiagnose.add(
                        Enkeltdiagnose().apply {
                            diagnose = "Vondt i skulder"
                            kodeverdi = diagnosekode
                            sortering = BigInteger.valueOf(0)
                        },
                    )
                }
            symptomerBehandling = "Får vondt av behandlingen"
            statusPresens = statuspresens
            vurderingYrkesskade =
                VurderingYrkesskade().apply {
                    borVurderes = BigInteger.valueOf(1)
                    skadeDato = convertToXmlGregorianCalendar(LocalDate.now())
                }
        }
    }

    private fun pasientopplysninger(pasientfnr: String, pdlPerson: PdlPerson): Pasientopplysninger {
        return Pasientopplysninger().apply {
            pasient =
                Pasient().apply {
                    fodselsnummer = pasientfnr
                    navn =
                        TypeNavn().apply {
                            fornavn = pdlPerson.navn.fornavn
                            etternavn = pdlPerson.navn.etternavn
                        }
                    arbeidsforhold =
                        Arbeidsforhold().apply {
                            primartArbeidsforhold = BigInteger.valueOf(1)
                            virksomhet =
                                Virksomhet().apply {
                                    organisasjonsnummer = "133144"
                                    virksomhetsBetegnelse = "NAV IKT"
                                    virksomhetsAdr =
                                        TypeAdresse().apply { adressetype = TypeAdressetype.ABC }
                                }
                            yrkeskode = "Utvikler"
                        }
                }
        }
    }

    private fun opprettHealthcareProfessional(
        legeFnr: String,
        pdlPerson: PdlPerson
    ): XMLHealthcareProfessional {
        return XMLHealthcareProfessional().apply {
            ident.add(
                XMLIdent().apply {
                    id = legeFnr
                    typeId =
                        XMLCV().apply {
                            v = "FNR"
                            s = "6.87.654.3.21.9.8.7.6543.2198"
                            dn = "Fødselsnummer"
                        }
                },
            )
            familyName = pdlPerson.navn.etternavn
            middleName = pdlPerson.navn.mellomnavn
            givenName = pdlPerson.navn.fornavn
        }
    }
}
