package no.nav.syfo.dolly.api

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.syfo.dolly.DollyClient
import no.nav.syfo.dolly.model.DollyResponse
import no.nav.syfo.dolly.model.DollySykmelding
import no.nav.syfo.dolly.model.DollySykmeldingResponse
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.logger
import org.koin.ktor.ext.inject

fun Route.registrerDollySykmeldingApi() {
    val dollyClient: DollyClient by inject()

    post("/sykmelding") {
        val request = call.receive<DollySykmelding>()
        logger.info("DollySykmelding: $request")

        val opprettSykmelding: DollyResponse<Unit> = dollyClient.opprettSykmelding(request)
        call.respond(opprettSykmelding.status, HttpMessage(opprettSykmelding.message))
    }
    get("/sykmelding/{sykmeldingId}") {
        val sykmeldingId = call.parameters["sykmeldingId"]
        requireNotNull(sykmeldingId)
        logger.info("Henter sykmelding fra input-dolly med sykmeldingId $sykmeldingId")

        val hentSykmelding: DollyResponse<DollySykmeldingResponse> =
            dollyClient.hentSykmelding(sykmeldingId)
        if (hentSykmelding.data != null) {
            call.respond(hentSykmelding.status, hentSykmelding.data)
        } else {
            call.respond(hentSykmelding.status, HttpMessage(hentSykmelding.message))
        }
    }
    delete("/sykmelding/ident") {
        val ident = call.request.headers["Sykmeldt-Fnr"]
        requireNotNull(ident)

        val slettSykmeldiger: DollyResponse<Unit> = dollyClient.slettSykmeldinger(ident)
        call.respond(slettSykmeldiger.status, HttpMessage(slettSykmeldiger.message))
    }
}
