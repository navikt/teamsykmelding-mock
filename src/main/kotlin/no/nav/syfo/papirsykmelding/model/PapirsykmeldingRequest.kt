package no.nav.syfo.papirsykmelding.model

import java.time.LocalDate
import no.nav.syfo.model.SykmeldingPeriode

data class PapirsykmeldingRequest(
    val fnr: String?,
    val hprNummer: String,
    val syketilfelleStartdato: LocalDate,
    val behandletDato: LocalDate,
    val perioder: List<SykmeldingPeriode>,
    val diagnosekode: String,
    val diagnosekodesystem: String,
    val utenOcr: Boolean,
)
