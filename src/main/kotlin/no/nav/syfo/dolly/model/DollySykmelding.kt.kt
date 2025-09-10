package no.nav.syfo.dolly.model

import java.time.LocalDate

data class Aktivitet(
    val fom: LocalDate,
    val tom: LocalDate,
)

data class DollySykmelding(
    val ident: String,
    val aktivitet: List<Aktivitet>,
)

data class DollySykmeldingResponse(
    val sykmeldingId: String,
    val ident: String,
    val aktivitet: List<Aktivitet>,
)
