package no.nav.syfo.sykmelding

import no.nav.syfo.model.sykmeldingstatus.STATUS_SLETTET
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.sykmelding.kafka.SykmeldingStatusKafkaProducer
import no.nav.syfo.sykmelding.kafka.TombstoneKafkaProducer
import java.time.OffsetDateTime
import java.time.ZoneOffset

class SlettSykmeldingService(
    private val sykmeldingStatusKafkaProducer: SykmeldingStatusKafkaProducer,
    private val tombstoneKafkaProducer: TombstoneKafkaProducer
) {
    fun slettSykmelding(sykmeldingId: String, fnr: String) {
        sykmeldingStatusKafkaProducer.send(
            SykmeldingStatusKafkaEventDTO(
                sykmeldingId = sykmeldingId,
                timestamp = OffsetDateTime.now(ZoneOffset.UTC),
                statusEvent = STATUS_SLETTET,
                arbeidsgiver = null,
                sporsmals = null
            ),
            fnr
        )
        tombstoneKafkaProducer.sendTombstone(sykmeldingId)
    }
}
