package no.nav.syfo.narmesteleder.kafka

import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.narmesteleder.kafka.model.KafkaMetadata
import no.nav.syfo.narmesteleder.kafka.model.NlAvbrutt
import no.nav.syfo.narmesteleder.kafka.model.NlResponse
import no.nav.syfo.narmesteleder.kafka.model.NlResponseKafkaMessage
import no.nav.syfo.utils.JacksonKafkaSerializer
import no.nav.syfo.utils.logger
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

interface NlResponseProducer {
    fun sendNlResponse(nlResponse: NlResponse?, nlAvbrutt: NlAvbrutt?)
}

class NlResponseProducerProduction(private val topic: String) : NlResponseProducer {
    private val nlResponseProducer =
        KafkaProducer<String, NlResponseKafkaMessage>(
            KafkaUtils.getAivenKafkaConfig("nl-response-producer")
                .toProducerConfig(
                    "mock-nl-response-producer",
                    JacksonKafkaSerializer::class,
                )
        )

    override fun sendNlResponse(nlResponse: NlResponse?, nlAvbrutt: NlAvbrutt?) {
        println("sendNlResponse called with nlResponse: $nlResponse, nlAvbrutt: $nlAvbrutt")
        val kafkaMessage =
            NlResponseKafkaMessage(
                kafkaMetadata = KafkaMetadata(OffsetDateTime.now(ZoneOffset.UTC), "mock"),
                nlResponse = nlResponse,
                nlAvbrutt = nlAvbrutt,
            )
        nlResponseProducer
            .send(
                ProducerRecord(topic, nlResponse?.orgnummer ?: nlAvbrutt?.orgnummer, kafkaMessage)
            )
            .get()
    }
}

class NlResponseProducerDevelopment() : NlResponseProducer {
    override fun sendNlResponse(nlResponse: NlResponse?, nlAvbrutt: NlAvbrutt?) {
        logger.info("sending NL response: $nlResponse, nlAvbrutt: $nlAvbrutt")
    }
}
