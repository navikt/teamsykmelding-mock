package no.nav.syfo.sykmelding.model

import no.nav.syfo.model.SykmeldingPeriode
import java.time.LocalDate

data class SykmeldingRequest(
    val fnr: String,
    val fnrLege: String,
    val herId: String?,
    val meldingTilArbeidsgiver: String?,
    val hprNummer: String?,
    val syketilfelleStartdato: LocalDate,
    val diagnosekode: String,
    val diagnosekodesystem: String,
    val annenFraverGrunn: AnnenFraverGrunn?,
    val perioder: List<SykmeldingPeriode>,
    val behandletDato: LocalDate,
    val kontaktDato: LocalDate?,
    val begrunnIkkeKontakt: String?,
    val vedlegg: Boolean,
    val virksomhetsykmelding: Boolean,
    val utenUtdypendeOpplysninger: Boolean,
    val regelsettVersjon: String?,
    val bidiagnoser: List<Diagnoser>?,
    val arbeidsgiverNavn: String?,
)

enum class AnnenFraverGrunn(val codeValue: String, val text: String) {
    GODKJENT_HELSEINSTITUSJON("1", "Når vedkommende er innlagt i en godkjent helseinstitusjon"),
    BEHANDLING_FORHINDRER_ARBEID("2", "Når vedkommende er under behandling og legen erklærer at behandlingen gjør det nødvendig at vedkommende ikke arbeider"),
    ARBEIDSRETTET_TILTAK("3", "Når vedkommende deltar på et arbeidsrettet tiltak"),
    MOTTAR_TILSKUDD_GRUNNET_HELSETILSTAND("4", "Når vedkommende på grunn av sykdom, skade eller lyte får tilskott når vedkommende på grunn av sykdom, skade eller lyte får tilskott"),
    NODVENDIG_KONTROLLUNDENRSOKELSE("5", "Når vedkommende er til nødvendig kontrollundersøkelse som krever minst 24 timers fravær, reisetid medregnet"),
    SMITTEFARE("6", "Når vedkommende myndighet har nedlagt forbud mot at han eller hun arbeider på grunn av smittefare"),
    ABORT("7", "Når vedkommende er arbeidsufør som følge av svangerskapsavbrudd"),
    UFOR_GRUNNET_BARNLOSHET("8", "Når vedkommende er arbeidsufør som følge av behandling for barnløshet"),
    DONOR("9", "Når vedkommende er donor eller er under vurdering som donor"),
    BEHANDLING_STERILISERING("10", "Når vedkommende er arbeidsufør som følge av behandling i forbindelse med sterilisering")
}

data class Diagnoser(
    val code: String,
    val system: String,
    val text: String
)
