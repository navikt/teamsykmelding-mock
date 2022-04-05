package no.nav.syfo.sykmelding.kafka

import no.nav.syfo.log
import no.nav.syfo.model.sykmeldingstatus.KafkaMetadataDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaMessageDTO
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.OffsetDateTime
import java.time.ZoneOffset

class SykmeldingStatusKafkaProducer(
    private val kafkaProducer: KafkaProducer<String, SykmeldingStatusKafkaMessageDTO>,
    private val statusTopic: String
) {
    fun send(sykmeldingStatusKafkaEventDTO: SykmeldingStatusKafkaEventDTO, fnr: String) {
        log.info("Skriver slettet-status for sykmelding med id ${sykmeldingStatusKafkaEventDTO.sykmeldingId}")
        val metadataDTO = KafkaMetadataDTO(
            sykmeldingId = sykmeldingStatusKafkaEventDTO.sykmeldingId,
            timestamp = OffsetDateTime.now(
                ZoneOffset.UTC
            ),
            fnr = fnr,
            source = "teamsykmelding-mock-backend"
        )

        val sykmeldingStatusKafkaMessageDTO = SykmeldingStatusKafkaMessageDTO(metadataDTO, sykmeldingStatusKafkaEventDTO)

        try {
            kafkaProducer.send(ProducerRecord(statusTopic, sykmeldingStatusKafkaMessageDTO.event.sykmeldingId, sykmeldingStatusKafkaMessageDTO)).get()
        } catch (ex: Exception) {
            log.error("Kunne ikke sende slettet-melding til topic", ex)
            throw ex
        }
    }
}
