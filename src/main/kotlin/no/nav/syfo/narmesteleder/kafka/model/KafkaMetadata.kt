package no.nav.syfo.narmesteleder.kafka.model

import java.time.OffsetDateTime

data class KafkaMetadata(
    val timestamp: OffsetDateTime,
    val source: String
)
