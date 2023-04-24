package no.nav.syfo.narmesteleder.kafka

import no.nav.syfo.narmesteleder.kafka.model.KafkaMetadata
import no.nav.syfo.narmesteleder.kafka.model.NlAvbrutt
import no.nav.syfo.narmesteleder.kafka.model.NlResponse
import no.nav.syfo.narmesteleder.kafka.model.NlResponseKafkaMessage
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.OffsetDateTime
import java.time.ZoneOffset

class NlResponseProducer(private val kafkaProducer: KafkaProducer<String, NlResponseKafkaMessage>, private val topic: String) {
    fun sendNlResponse(nlResponse: NlResponse?, nlAvbrutt: NlAvbrutt?) {
        val kafkaMessage = NlResponseKafkaMessage(
            kafkaMetadata = KafkaMetadata(OffsetDateTime.now(ZoneOffset.UTC), "mock"),
            nlResponse = nlResponse,
            nlAvbrutt = nlAvbrutt,
        )
        kafkaProducer.send(ProducerRecord(topic, nlResponse?.orgnummer ?: nlAvbrutt?.orgnummer, kafkaMessage)).get()
    }
}
