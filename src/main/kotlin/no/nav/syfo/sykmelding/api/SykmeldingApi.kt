package no.nav.syfo.sykmelding.api

import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.sykmelding.SlettSykmeldingService
import no.nav.syfo.sykmelding.SykmeldingService
import no.nav.syfo.sykmelding.model.SykmeldingRequest
import no.nav.syfo.utils.logger
import org.koin.ktor.ext.inject

fun Route.registrerSykmeldingApi() {
    val sykmeldingService by inject<SykmeldingService>()
    val slettSykmeldingService by inject<SlettSykmeldingService>()
    post("/sykmelding/opprett") {
        val request = call.receive<SykmeldingRequest>()
        logger.info("sykmeldingRequesten fra frontend: $request")

        if (request.fnr.length != 11) {
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage("request.fnr har feil lengde, er ${request.fnr.length}")
            )
            return@post
        }
        val mottakId = sykmeldingService.opprettSykmelding(request)

        logger.info("Opprettet sykmelding")
        call.respond(HttpStatusCode.OK, HttpMessage("Opprettet sykmelding med mottakId $mottakId"))
    }

    post("/sykmelding/regelsjekk") {
        val request = call.receive<SykmeldingRequest>()

        logger.info("Sjekker regler for sykmelding")
        val validationResult = sykmeldingService.sjekkRegler(request)

        logger.info("Har sjekket regler for sykmelding")
        call.respond(validationResult)
    }

    delete("/sykmeldinger") {
        logger.info("going to delete sykmeldinger")
        val fnr = call.request.headers["Sykmeldt-Fnr"]
        if (fnr == null || fnr.length != 11) {
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage("Sykmeldt-Fnr mangler eller har feil lengde")
            )
            return@delete
        }
        val antallSlettede = slettSykmeldingService.slettAlleSykmeldinger(fnr = fnr)
        logger.info("Slettet $antallSlettede sykmeldinger")
        call.respond(HttpStatusCode.OK, HttpMessage("Slettet $antallSlettede sykmeldinger"))
    }
}
