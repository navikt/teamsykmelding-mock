package no.nav.syfo.sykmelding.kafka

import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.utils.JacksonNullableKafkaSerializer
import no.nav.syfo.utils.logger
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

interface TombstoneKafkaProducer {
    fun sendTombstone(sykmeldingId: String)
}

class TombstoneKafkaProducerProduction(
    private val topics: List<String>,
) : TombstoneKafkaProducer {
    private val tombstoneProducer =
        KafkaProducer<String, Any?>(
            KafkaUtils.getAivenKafkaConfig("tombstone-producer")
                .toProducerConfig("mock-tombstone-producer", JacksonNullableKafkaSerializer::class),
        )

    override fun sendTombstone(sykmeldingId: String) {
        try {
            topics.forEach { topic ->
                tombstoneProducer.send(ProducerRecord(topic, sykmeldingId, null)).get()
            }
        } catch (e: Exception) {
            logger.error(
                "Kunne ikke skrive tombstone til topic for sykmeldingid $sykmeldingId: {}",
                e.message
            )
            throw e
        }
    }
}

class TombstoneKafkaProducerDevelopment() : TombstoneKafkaProducer {
    override fun sendTombstone(sykmeldingId: String) {
        logger.info("later som vi sender tombstone til topic for sykmeldingid $sykmeldingId")
    }
}
