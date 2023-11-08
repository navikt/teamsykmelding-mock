package no.nav.syfo.papirsykmelding

import com.migesok.jaxb.adapter.javatime.LocalDateXmlAdapter
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.math.BigInteger
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Base64
import java.util.UUID
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import no.nav.helse.msgHead.XMLMsgHead
import no.nav.helse.papirsykemelding.AktivitetIkkeMuligType
import no.nav.helse.papirsykemelding.AktivitetType
import no.nav.helse.papirsykemelding.AvventendeSykmeldingType
import no.nav.helse.papirsykemelding.BehandlingsdagerType
import no.nav.helse.papirsykemelding.GradertSykmeldingType
import no.nav.helse.papirsykemelding.HovedDiagnoseType
import no.nav.helse.papirsykemelding.KontaktMedPasientType
import no.nav.helse.papirsykemelding.ReisetilskuddType
import no.nav.helse.papirsykemelding.Skanningmetadata
import no.nav.helse.sm2013.HelseOpplysningerArbeidsuforhet
import no.nav.syfo.logger
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.SykmeldingPeriode
import no.nav.syfo.model.SykmeldingType
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.papirsykmelding.client.DokarkivClient
import no.nav.syfo.papirsykmelding.client.NorskHelsenettClient
import no.nav.syfo.papirsykmelding.client.SyfosmpapirreglerClient
import no.nav.syfo.papirsykmelding.client.opprettJournalpostPayload
import no.nav.syfo.papirsykmelding.client.opprettUtenlandskJournalpostPayload
import no.nav.syfo.papirsykmelding.model.PapirsykmeldingMappingException
import no.nav.syfo.papirsykmelding.model.PapirsykmeldingRequest
import no.nav.syfo.sykmelding.toSykmelding
import no.nav.syfo.util.XMLDateAdapter
import no.nav.syfo.util.get
import no.nav.syfo.util.jaxbContextSkanningmetadata
import no.nav.syfo.util.marshallFellesformat

class PapirsykmeldingService(
    private val dokarkivClient: DokarkivClient,
    private val syfosmpapirreglerClient: SyfosmpapirreglerClient,
    private val norskHelsenettClient: NorskHelsenettClient,
) {
    private val defaultPdf =
        PapirsykmeldingService::class
            .java
            .getResource("/papirsykmelding/base64Papirsykmelding")
            .readText(charset = Charsets.ISO_8859_1)
    private val utenlandskPdf =
        PapirsykmeldingService::class
            .java
            .getResource("/papirsykmelding/base64utenlandsk")
            .readText(charset = Charsets.ISO_8859_1)
    val defaultMetadata =
        "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2thbm5pbmdtZXRhZGF0YSB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4c2k6bm9OYW1lc3BhY2VTY2hlbWFMb2NhdGlvbj0ic2thbm5pbmdfbWV0YS54c2QiPgogICA8c3lrZW1lbGRpbmdlcj4KICAgICAgPHBhc2llbnQ+CiAgICAgICAgIDxmbnI+MDQwNTkwMzIxMzA8L2Zucj4KICAgICAgPC9wYXNpZW50PgogICAgICA8bWVkaXNpbnNrVnVyZGVyaW5nPgogICAgICAgICA8aG92ZWREaWFnbm9zZT4KICAgICAgICAgICAgPGRpYWdub3Nla29kZT45MTMuNDwvZGlhZ25vc2Vrb2RlPgogICAgICAgICAgICA8ZGlhZ25vc2U+Rm9yc3R1dmluZyBvZyBmb3JzdHJla2tpbmcgaSBjZXJ2aWthbGtvbHVtbmE8L2RpYWdub3NlPgogICAgICAgICA8L2hvdmVkRGlhZ25vc2U+CiAgICAgIDwvbWVkaXNpbnNrVnVyZGVyaW5nPgogICAgICA8YWt0aXZpdGV0PgogICAgICAgICA8YWt0aXZpdGV0SWtrZU11bGlnPgogICAgICAgICAgICA8cGVyaW9kZUZPTURhdG8+MjAxOS0wMS0xMDwvcGVyaW9kZUZPTURhdG8+CiAgICAgICAgICAgIDxwZXJpb2RlVE9NRGF0bz4yMDE5LTAxLTE0PC9wZXJpb2RlVE9NRGF0bz4KICAgICAgICAgICAgPG1lZGlzaW5za2VBcnNha2VyPgogICAgICAgICAgICAgICA8bWVkQXJzYWtlckhpbmRyZXI+MTwvbWVkQXJzYWtlckhpbmRyZXI+CiAgICAgICAgICAgIDwvbWVkaXNpbnNrZUFyc2FrZXI+CiAgICAgICAgIDwvYWt0aXZpdGV0SWtrZU11bGlnPgogICAgICA8L2FrdGl2aXRldD4KICAgICAgPHRpbGJha2VkYXRlcmluZz4KICAgICAgICAgPHRpbGJha2ViZWdydW5uZWxzZT5Ta2FkZWxlZ2V2YWt0ZW4KT3J0b3BlZGlzayBhdmRlbGluZzwvdGlsYmFrZWJlZ3J1bm5lbHNlPgogICAgICA8L3RpbGJha2VkYXRlcmluZz4KICAgICAgPGtvbnRha3RNZWRQYXNpZW50PgogICAgICAgICA8YmVoYW5kbGV0RGF0bz4yMDE5LTAxLTExPC9iZWhhbmRsZXREYXRvPgogICAgICA8L2tvbnRha3RNZWRQYXNpZW50PgogICAgICA8YmVoYW5kbGVyPgogICAgICAgICA8SFBSPjEwMDIzMjQ1PC9IUFI+CiAgICAgIDwvYmVoYW5kbGVyPgogICA8L3N5a2VtZWxkaW5nZXI+Cjwvc2thbm5pbmdtZXRhZGF0YT4="

    suspend fun opprettPapirsykmelding(papirsykmeldingRequest: PapirsykmeldingRequest): String {
        val ocr =
            if (papirsykmeldingRequest.utenOcr) {
                null
            } else {
                val skanningmetadata = tilSkanningmetadata(papirsykmeldingRequest)
                Base64.getEncoder().encodeToString(skanningmetadataTilByteArray(skanningmetadata))
            }
        return dokarkivClient.opprettJournalpost(
            opprettJournalpostPayload(
                fnr = papirsykmeldingRequest.fnr,
                ocr = ocr,
                pdf = defaultPdf,
                metadata = defaultMetadata,
            ),
        )
    }

    suspend fun sjekkRegler(papirsykmeldingRequest: PapirsykmeldingRequest): ValidationResult {
        if (papirsykmeldingRequest.fnr == null) {
            throw RuntimeException(
                "Kan ikke validere regler mot papirsykmelding uten fødselsnummer"
            )
        }
        val skanningMetadata = tilSkanningmetadata(papirsykmeldingRequest)
        val sykmeldingId = UUID.randomUUID().toString()
        val receivedSykmelding: ReceivedSykmelding
        try {
            val fnrLege =
                norskHelsenettClient.finnBehandlerFnr(papirsykmeldingRequest.hprNummer)
                    ?: throw RuntimeException(
                        "Fant ikke behandler i HPR, kan ikke validere mot regler"
                    )
            val fellesformat =
                mapOcrFilTilFellesformat(
                    skanningmetadata = skanningMetadata,
                    hprNummer = papirsykmeldingRequest.hprNummer,
                    fnrLege = fnrLege,
                    sykmeldingId = sykmeldingId,
                    fnr = papirsykmeldingRequest.fnr,
                    journalpostId = "123",
                )

            val healthInformation =
                fellesformat.get<XMLMsgHead>().document[0].refDoc.content.any[0]
                    as HelseOpplysningerArbeidsuforhet
            val sykmelding =
                healthInformation.toSykmelding(
                    sykmeldingId = sykmeldingId,
                    pasientAktoerId = "",
                    legeAktoerId = "",
                    msgId = sykmeldingId,
                    signaturDato =
                        LocalDateTime.of(papirsykmeldingRequest.behandletDato, LocalTime.NOON),
                    behandlerFnr = fnrLege,
                    behandlerHprNr = papirsykmeldingRequest.hprNummer,
                )

            receivedSykmelding =
                ReceivedSykmelding(
                    sykmelding = sykmelding,
                    personNrPasient = papirsykmeldingRequest.fnr,
                    tlfPasient =
                        healthInformation.pasient.kontaktInfo.firstOrNull()?.teleAddress?.v,
                    personNrLege = fnrLege,
                    legeHprNr = papirsykmeldingRequest.hprNummer,
                    legeHelsepersonellkategori = null,
                    navLogId = sykmeldingId,
                    msgId = sykmeldingId,
                    legekontorOrgNr = null,
                    legekontorOrgName = "",
                    legekontorHerId = null,
                    legekontorReshId = null,
                    mottattDato =
                        LocalDateTime.now()
                            .atZone(ZoneId.systemDefault())
                            .withZoneSameInstant(ZoneOffset.UTC)
                            .toLocalDateTime(),
                    rulesetVersion = healthInformation.regelSettVersjon,
                    fellesformat = marshallFellesformat(fellesformat),
                    tssid = "",
                    merknader = null,
                    partnerreferanse = null,
                    vedlegg = emptyList(),
                    utenlandskSykmelding = null,
                )
        } catch (e: Exception) {
            logger.error("Kunne ikke mappe request til received sykmelding", e)
            throw PapirsykmeldingMappingException(e.message)
        }

        return syfosmpapirreglerClient.sjekkRegler(receivedSykmelding)
    }

    suspend fun opprettUtenlandskPapirsykmelding(fnr: String): String {
        return dokarkivClient.opprettJournalpost(
            opprettUtenlandskJournalpostPayload(
                fnr = fnr,
                pdf = utenlandskPdf,
                metadata = defaultMetadata,
            ),
        )
    }

    fun tilSkanningmetadata(papirsykmeldingRequest: PapirsykmeldingRequest): Skanningmetadata {
        val skanningmetadataUnmarshaller: Unmarshaller =
            jaxbContextSkanningmetadata.createUnmarshaller().apply {
                setAdapter(LocalDateXmlAdapter::class.java, XMLDateAdapter())
            }

        val skanningmetadataXml =
            PapirsykmeldingService::class
                .java
                .getResource("/papirsykmelding/skanningmetadata.xml")
                .readText(charset = Charsets.ISO_8859_1)
        val skanningmetadata =
            skanningmetadataUnmarshaller.unmarshal(StringReader(skanningmetadataXml))
                as Skanningmetadata

        skanningmetadata.sykemeldinger.pasient.fnr = papirsykmeldingRequest.fnr
        skanningmetadata.sykemeldinger.syketilfelleStartDato =
            papirsykmeldingRequest.syketilfelleStartdato
        skanningmetadata.sykemeldinger.behandler.hpr =
            papirsykmeldingRequest.hprNummer.toInt().toBigInteger()
        val hoveddiagnose = HovedDiagnoseType()
        hoveddiagnose.diagnosekode = papirsykmeldingRequest.diagnosekode
        hoveddiagnose.diagnosekodeSystem = papirsykmeldingRequest.diagnosekodesystem
        skanningmetadata.sykemeldinger.medisinskVurdering.hovedDiagnose.clear()
        skanningmetadata.sykemeldinger.medisinskVurdering.hovedDiagnose.add(hoveddiagnose)
        skanningmetadata.sykemeldinger.aktivitet = tilAktivitet(papirsykmeldingRequest.perioder)
        skanningmetadata.sykemeldinger.kontaktMedPasient =
            KontaktMedPasientType().apply { behandletDato = papirsykmeldingRequest.behandletDato }

        return skanningmetadata
    }

    private fun tilAktivitet(perioder: List<SykmeldingPeriode>): AktivitetType {
        val aktivitetType = AktivitetType()
        perioder.forEach {
            when (it.type) {
                SykmeldingType.HUNDREPROSENT ->
                    aktivitetType.aktivitetIkkeMulig =
                        AktivitetIkkeMuligType().apply {
                            periodeFOMDato = it.fom
                            periodeTOMDato = it.tom
                        }
                SykmeldingType.GRADERT_20 ->
                    aktivitetType.gradertSykmelding =
                        GradertSykmeldingType().apply {
                            periodeFOMDato = it.fom
                            periodeTOMDato = it.tom
                            sykmeldingsgrad = "20"
                        }
                SykmeldingType.GRADERT_40 ->
                    aktivitetType.gradertSykmelding =
                        GradertSykmeldingType().apply {
                            periodeFOMDato = it.fom
                            periodeTOMDato = it.tom
                            sykmeldingsgrad = "40"
                        }
                SykmeldingType.GRADERT_50 ->
                    aktivitetType.gradertSykmelding =
                        GradertSykmeldingType().apply {
                            periodeFOMDato = it.fom
                            periodeTOMDato = it.tom
                            sykmeldingsgrad = "50"
                        }
                SykmeldingType.GRADERT_60 ->
                    aktivitetType.gradertSykmelding =
                        GradertSykmeldingType().apply {
                            periodeFOMDato = it.fom
                            periodeTOMDato = it.tom
                            sykmeldingsgrad = "60"
                        }
                SykmeldingType.GRADERT_80 ->
                    aktivitetType.gradertSykmelding =
                        GradertSykmeldingType().apply {
                            periodeFOMDato = it.fom
                            periodeTOMDato = it.tom
                            sykmeldingsgrad = "80"
                        }
                SykmeldingType.AVVENTENDE -> {
                    aktivitetType.avventendeSykmelding =
                        AvventendeSykmeldingType().apply {
                            periodeFOMDato = it.fom
                            periodeTOMDato = it.tom
                        }
                    aktivitetType.innspillTilArbeidsgiver = "Masse fine innspill"
                }
                SykmeldingType.GRADERT_REISETILSKUDD ->
                    aktivitetType.gradertSykmelding =
                        GradertSykmeldingType().apply {
                            periodeFOMDato = it.fom
                            periodeTOMDato = it.tom
                            sykmeldingsgrad = "60"
                            isReisetilskudd = true
                        }
                SykmeldingType.BEHANDLINGSDAGER ->
                    aktivitetType.behandlingsdager =
                        BehandlingsdagerType().apply {
                            periodeFOMDato = it.fom
                            periodeTOMDato = it.tom
                            antallBehandlingsdager = BigInteger.valueOf(4)
                        }
                SykmeldingType.BEHANDLINGSDAG ->
                    aktivitetType.behandlingsdager =
                        BehandlingsdagerType().apply {
                            periodeFOMDato = it.fom
                            periodeTOMDato = it.tom
                            antallBehandlingsdager = BigInteger.valueOf(1)
                        }
                SykmeldingType.REISETILSKUDD ->
                    aktivitetType.reisetilskudd =
                        ReisetilskuddType().apply {
                            periodeFOMDato = it.fom
                            periodeTOMDato = it.tom
                        }
            }
        }
        return aktivitetType
    }

    private fun skanningmetadataTilByteArray(skanningmetadata: Skanningmetadata): ByteArray {
        val sykmeldingMarshaller: Marshaller =
            jaxbContextSkanningmetadata.createMarshaller().apply {
                setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
            }
        return ByteArrayOutputStream()
            .use {
                sykmeldingMarshaller.marshal(skanningmetadata, it)
                it
            }
            .toByteArray()
    }
}
