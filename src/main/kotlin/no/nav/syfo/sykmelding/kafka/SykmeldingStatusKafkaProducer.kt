package no.nav.syfo.sykmelding.kafka

import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.model.sykmeldingstatus.KafkaMetadataDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaMessageDTO
import no.nav.syfo.utils.JacksonKafkaSerializer
import no.nav.syfo.utils.logger
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

interface SykmeldingStatusKafkaProducer {
    fun send(sykmeldingStatusKafkaEventDTO: SykmeldingStatusKafkaEventDTO, fnr: String)
}

class SykmeldingStatusKafkaProducerProduction(
    private val statusTopic: String,
) : SykmeldingStatusKafkaProducer {
    private val kafkaProducer =
        KafkaProducer<String, SykmeldingStatusKafkaMessageDTO>(
            KafkaUtils.getAivenKafkaConfig("sykmelding-status-producer")
                .toProducerConfig(
                    groupId = "mock-sykmelding-status-producer",
                    valueSerializer = JacksonKafkaSerializer::class,
                    keySerializer = StringSerializer::class,
                ),
        )

    override fun send(sykmeldingStatusKafkaEventDTO: SykmeldingStatusKafkaEventDTO, fnr: String) {
        logger.info(
            "Skriver slettet-status for sykmelding med id ${sykmeldingStatusKafkaEventDTO.sykmeldingId}"
        )
        val metadataDTO =
            KafkaMetadataDTO(
                sykmeldingId = sykmeldingStatusKafkaEventDTO.sykmeldingId,
                timestamp =
                    OffsetDateTime.now(
                        ZoneOffset.UTC,
                    ),
                fnr = fnr,
                source = "teamsykmelding-mock-backend",
            )

        val sykmeldingStatusKafkaMessageDTO =
            SykmeldingStatusKafkaMessageDTO(metadataDTO, sykmeldingStatusKafkaEventDTO)

        try {
            kafkaProducer
                .send(
                    ProducerRecord(
                        statusTopic,
                        sykmeldingStatusKafkaMessageDTO.event.sykmeldingId,
                        sykmeldingStatusKafkaMessageDTO
                    )
                )
                .get()
        } catch (ex: Exception) {
            logger.error("Kunne ikke sende slettet-melding til topic", ex)
            throw ex
        }
    }
}

class SykmeldingStatusKafkaProducerDevelopment : SykmeldingStatusKafkaProducer {
    override fun send(sykmeldingStatusKafkaEventDTO: SykmeldingStatusKafkaEventDTO, fnr: String) {
        logger.info(
            "Later som vi skriver statusendring for sykmelding med id {} til topic",
            sykmeldingStatusKafkaEventDTO.sykmeldingId,
        )
    }
}
