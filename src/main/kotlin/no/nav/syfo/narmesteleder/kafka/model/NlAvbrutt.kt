package no.nav.syfo.narmesteleder.kafka.model

import java.time.OffsetDateTime

data class NlAvbrutt(
    val orgnummer: String,
    val sykmeldtFnr: String,
    val aktivTom: OffsetDateTime
)
