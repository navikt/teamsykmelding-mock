package no.nav.syfo.papirsykmelding.model

import no.nav.syfo.model.SykmeldingPeriode
import java.time.LocalDate

data class PapirsykmeldingRequest(
    val fnr: String,
    val hprNummer: String,
    val syketilfelleStartdato: LocalDate,
    val perioder: List<SykmeldingPeriode>,
    val diagnosekode: String,
    val diagnosekodesystem: String,
    val utenOcr: Boolean
)
