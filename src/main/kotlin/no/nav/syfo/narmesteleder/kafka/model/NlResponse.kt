package no.nav.syfo.narmesteleder.kafka.model

data class NlResponse(
    val orgnummer: String,
    val utbetalesLonn: Boolean?,
    val leder: Leder,
    val sykmeldt: Sykmeldt
)
