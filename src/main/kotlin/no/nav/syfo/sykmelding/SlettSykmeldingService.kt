package no.nav.syfo.sykmelding

import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.model.sykmeldingstatus.STATUS_SLETTET
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.sykmelding.client.SyfosmregisterClient
import no.nav.syfo.sykmelding.kafka.SykmeldingStatusKafkaProducer
import no.nav.syfo.sykmelding.kafka.TombstoneKafkaProducer
import no.nav.syfo.utils.logger

class SlettSykmeldingService(
    private val syfosmregisterClient: SyfosmregisterClient,
    private val sykmeldingStatusKafkaProducer: SykmeldingStatusKafkaProducer,
    private val tombstoneKafkaProducer: TombstoneKafkaProducer,
) {
    suspend fun slettAlleSykmeldinger(fnr: String): Int {
        logger.info("Henter ut alle sykmeldinger fra registeret")
        val sykmeldinger = syfosmregisterClient.hentSykmeldinger(fnr)
        logger.info("Sletter ${sykmeldinger.size} sykmeldinger")

        sykmeldinger.forEach {
            sykmeldingStatusKafkaProducer.send(
                SykmeldingStatusKafkaEventDTO(
                    sykmeldingId = it.id,
                    timestamp = OffsetDateTime.now(ZoneOffset.UTC),
                    statusEvent = STATUS_SLETTET,
                    arbeidsgiver = null,
                    sporsmals = null,
                ),
                fnr,
            )
            tombstoneKafkaProducer.sendTombstone(it.id)
        }
        return sykmeldinger.size
    }
}
