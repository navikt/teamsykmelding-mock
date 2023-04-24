package no.nav.syfo.sykmelding.kafka

import no.nav.syfo.log
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class TombstoneKafkaProducer(
    private val tombstoneProducer: KafkaProducer<String, Any?>,
    private val topics: List<String>,
) {
    fun sendTombstone(sykmeldingId: String) {
        try {
            topics.forEach { topic ->
                tombstoneProducer.send(ProducerRecord(topic, sykmeldingId, null)).get()
            }
        } catch (e: Exception) {
            log.error("Kunne ikke skrive tombstone til topic for sykmeldingid $sykmeldingId: {}", e.message)
            throw e
        }
    }
}
