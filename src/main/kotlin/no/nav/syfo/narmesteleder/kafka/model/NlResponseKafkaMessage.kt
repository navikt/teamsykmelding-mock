package no.nav.syfo.narmesteleder.kafka.model

data class NlResponseKafkaMessage(
    val kafkaMetadata: KafkaMetadata,
    val nlResponse: NlResponse
)
