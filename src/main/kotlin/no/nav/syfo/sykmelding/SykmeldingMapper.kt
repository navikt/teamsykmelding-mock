package no.nav.syfo.sykmelding

import java.time.LocalDateTime
import no.nav.helse.sm2013.Address
import no.nav.helse.sm2013.ArsakType
import no.nav.helse.sm2013.CS
import no.nav.helse.sm2013.CV
import no.nav.helse.sm2013.HelseOpplysningerArbeidsuforhet
import no.nav.helse.sm2013.TeleCom
import no.nav.syfo.model.Adresse
import no.nav.syfo.model.AktivitetIkkeMulig
import no.nav.syfo.model.AnnenFraverGrunn
import no.nav.syfo.model.AnnenFraversArsak
import no.nav.syfo.model.Arbeidsgiver
import no.nav.syfo.model.ArbeidsrelatertArsak
import no.nav.syfo.model.ArbeidsrelatertArsakType
import no.nav.syfo.model.AvsenderSystem
import no.nav.syfo.model.Behandler
import no.nav.syfo.model.Diagnose
import no.nav.syfo.model.ErIArbeid
import no.nav.syfo.model.ErIkkeIArbeid
import no.nav.syfo.model.Gradert
import no.nav.syfo.model.HarArbeidsgiver
import no.nav.syfo.model.KontaktMedPasient
import no.nav.syfo.model.MedisinskArsak
import no.nav.syfo.model.MedisinskArsakType
import no.nav.syfo.model.MedisinskVurdering
import no.nav.syfo.model.MeldingTilNAV
import no.nav.syfo.model.Periode
import no.nav.syfo.model.Prognose
import no.nav.syfo.model.SporsmalSvar
import no.nav.syfo.model.SvarRestriksjon
import no.nav.syfo.model.Sykmelding

fun HelseOpplysningerArbeidsuforhet.toSykmelding(
    sykmeldingId: String,
    pasientAktoerId: String,
    legeAktoerId: String,
    msgId: String,
    signaturDato: LocalDateTime,
    behandlerFnr: String,
    behandlerHprNr: String?,
) =
    Sykmelding(
        id = sykmeldingId,
        msgId = msgId,
        pasientAktoerId = pasientAktoerId,
        medisinskVurdering = medisinskVurdering.toMedisinskVurdering(),
        skjermesForPasient = medisinskVurdering?.isSkjermesForPasient ?: false,
        arbeidsgiver = arbeidsgiver.toArbeidsgiver(),
        perioder =
            aktivitet.periode.map(HelseOpplysningerArbeidsuforhet.Aktivitet.Periode::toPeriode),
        prognose = prognose?.toPrognose(),
        utdypendeOpplysninger = utdypendeOpplysninger?.toMap() ?: mapOf(),
        tiltakArbeidsplassen = tiltak?.tiltakArbeidsplassen,
        tiltakNAV = tiltak?.tiltakNAV,
        andreTiltak = tiltak?.andreTiltak,
        meldingTilNAV = meldingTilNav?.toMeldingTilNAV(regelSettVersjon),
        meldingTilArbeidsgiver = meldingTilArbeidsgiver,
        kontaktMedPasient = kontaktMedPasient.toKontaktMedPasient(),
        behandletTidspunkt = kontaktMedPasient.behandletDato,
        behandler = behandler.toBehandler(legeAktoerId, behandlerFnr, behandlerHprNr),
        avsenderSystem = avsenderSystem.toAvsenderSystem(),
        syketilfelleStartDato = syketilfelleStartDato,
        signaturDato = signaturDato,
        navnFastlege = pasient?.navnFastlege,
    )

fun HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.toPeriode() =
    Periode(
        fom = periodeFOMDato,
        tom = periodeTOMDato,
        aktivitetIkkeMulig = aktivitetIkkeMulig?.toAktivitetIkkeMulig(),
        avventendeInnspillTilArbeidsgiver = avventendeSykmelding?.innspillTilArbeidsgiver,
        behandlingsdager = behandlingsdager?.antallBehandlingsdagerUke,
        gradert = gradertSykmelding?.toGradert(),
        reisetilskudd = isReisetilskudd == true,
    )

fun HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.GradertSykmelding.toGradert() =
    Gradert(
        reisetilskudd = isReisetilskudd == true,
        grad = sykmeldingsgrad,
    )

fun HelseOpplysningerArbeidsuforhet.Arbeidsgiver.toArbeidsgiver() =
    Arbeidsgiver(
        harArbeidsgiver = HarArbeidsgiver.values().first { it.codeValue == harArbeidsgiver.v },
        navn = navnArbeidsgiver,
        yrkesbetegnelse = yrkesbetegnelse,
        stillingsprosent = stillingsprosent,
    )

fun HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.AktivitetIkkeMulig.toAktivitetIkkeMulig() =
    AktivitetIkkeMulig(
        medisinskArsak = medisinskeArsaker?.toMedisinskArsak(),
        arbeidsrelatertArsak = arbeidsplassen?.toArbeidsrelatertArsak(),
    )

fun HelseOpplysningerArbeidsuforhet.MedisinskVurdering.toMedisinskVurdering() =
    MedisinskVurdering(
        hovedDiagnose = hovedDiagnose?.diagnosekode?.toDiagnose(),
        biDiagnoser = biDiagnoser?.diagnosekode?.map(CV::toDiagnose) ?: listOf(),
        svangerskap = isSvangerskap == true,
        yrkesskade = isYrkesskade == true,
        yrkesskadeDato = yrkesskadeDato,
        annenFraversArsak = annenFraversArsak?.toAnnenFraversArsak(),
    )

fun CV.toDiagnose() =
    if (v.contains(".")) {
        Diagnose(s, v.replace(".", ""), dn)
    } else {
        Diagnose(s, v, dn)
    }

fun ArsakType.toAnnenFraversArsak() =
    AnnenFraversArsak(
        beskrivelse = beskriv,
        // TODO: Remove if-wrapping whenever the EPJ systems stops sending garbage data
        grunn =
            arsakskode.mapNotNull { code ->
                if (code.v == "0") {
                    null
                } else {
                    AnnenFraverGrunn.values().first { it.codeValue == code.v.trim() }
                }
            },
    )

// TODO: Remove if-wrapping whenever the EPJ systems stops sending garbage data
fun CS.toMedisinskArsakType() =
    if (v == "0") {
        null
    } else {
        MedisinskArsakType.values().first { it.codeValue == v.trim() }
    }

// TODO: Remove if-wrapping whenever the EPJ systems stops sending garbage data
fun CS.toArbeidsrelatertArsakType() =
    if (v == "0") {
        null
    } else {
        ArbeidsrelatertArsakType.values().first { it.codeValue == v }
    }

fun HelseOpplysningerArbeidsuforhet.Prognose.toPrognose() =
    Prognose(
        arbeidsforEtterPeriode = isArbeidsforEtterEndtPeriode == true,
        hensynArbeidsplassen = beskrivHensynArbeidsplassen,
        erIArbeid =
            erIArbeid?.let {
                ErIArbeid(
                    egetArbeidPaSikt = it.isEgetArbeidPaSikt == true,
                    annetArbeidPaSikt = it.isAnnetArbeidPaSikt == true,
                    arbeidFOM = it.arbeidFraDato,
                    vurderingsdato = it.vurderingDato,
                )
            },
        erIkkeIArbeid =
            erIkkeIArbeid?.let {
                ErIkkeIArbeid(
                    arbeidsforPaSikt = it.isArbeidsforPaSikt == true,
                    arbeidsforFOM = it.arbeidsforFraDato,
                    vurderingsdato = it.vurderingDato,
                )
            },
    )

// TODO: Remove mapNotNull whenever the EPJ systems stops sending garbage data
fun HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger.toMap() =
    spmGruppe
        .map { spmGruppe ->
            spmGruppe.spmGruppeId to
                spmGruppe.spmSvar
                    .map { svar ->
                        svar.spmId to
                            SporsmalSvar(
                                sporsmal = svar.spmTekst,
                                svar = svar.svarTekst,
                                restriksjoner =
                                    svar.restriksjon
                                        ?.restriksjonskode
                                        ?.mapNotNull(CS::toSvarRestriksjon)
                                        ?: listOf()
                            )
                    }
                    .toMap()
        }
        .toMap()

// TODO: Remove if-wrapping whenever the EPJ systems stops sending garbage data
fun CS.toSvarRestriksjon() =
    if (v.isNullOrBlank()) {
        null
    } else {
        SvarRestriksjon.values().first { it.codeValue == v }
    }

fun Address.toAdresse() =
    Adresse(
        gate = streetAdr,
        postnummer = postalCode?.toIntOrNull(),
        kommune = city,
        postboks = postbox,
        land = country?.v,
    )

// TODO: Remove mapNotNull whenever the EPJ systems stops sending garbage data
fun ArsakType.toArbeidsrelatertArsak() =
    ArbeidsrelatertArsak(
        beskrivelse = beskriv,
        arsak = arsakskode.mapNotNull(CS::toArbeidsrelatertArsakType),
    )

// TODO: Remove mapNotNull whenever the EPJ systems stops sending garbage data
fun ArsakType.toMedisinskArsak() =
    MedisinskArsak(
        beskrivelse = beskriv,
        arsak = arsakskode.mapNotNull(CS::toMedisinskArsakType),
    )

fun HelseOpplysningerArbeidsuforhet.MeldingTilNav.toMeldingTilNAV(rulesetVersion: String?) =
    MeldingTilNAV(
        bistandUmiddelbart =
            if (rulesetVersion == "3" && !beskrivBistandNAV.isNullOrEmpty()) {
                true
            } else {
                isBistandNAVUmiddelbart
            }
                ?: false,
        beskrivBistand = beskrivBistandNAV,
    )

fun HelseOpplysningerArbeidsuforhet.KontaktMedPasient.toKontaktMedPasient() =
    KontaktMedPasient(
        kontaktDato = kontaktDato,
        begrunnelseIkkeKontakt = begrunnIkkeKontakt,
    )

fun HelseOpplysningerArbeidsuforhet.Behandler.toBehandler(
    aktoerId: String,
    behandlerFnr: String,
    behandlerHprNr: String?
) =
    Behandler(
        fornavn = navn.fornavn,
        mellomnavn = navn.mellomnavn,
        etternavn = navn.etternavn,
        aktoerId = aktoerId,
        fnr = behandlerFnr,
        hpr = behandlerHprNr ?: id.find { it.typeId.v == "HPR" }?.id,
        her = id.find { it.typeId.v == "HER" }?.id,
        adresse = adresse.toAdresse(),
        tlf = extractTlfFromKontaktInfo(kontaktInfo),
    )

fun HelseOpplysningerArbeidsuforhet.AvsenderSystem.toAvsenderSystem() =
    AvsenderSystem(
        navn = systemNavn,
        versjon = systemVersjon,
    )

fun extractTlfFromKontaktInfo(kontaktInfo: List<TeleCom>?): String? =
    if (
        kontaktInfo?.size != 0 &&
            kontaktInfo?.firstOrNull()!!.teleAddress != null &&
            kontaktInfo.firstOrNull()!!.teleAddress?.v?.contains("tel:") == true
    ) {
        kontaktInfo.firstOrNull()!!.teleAddress?.v?.removePrefix("tel:")
    } else {
        kontaktInfo.firstOrNull()?.teleAddress?.v
    }
