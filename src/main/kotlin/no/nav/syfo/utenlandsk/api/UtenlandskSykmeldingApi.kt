package no.nav.syfo.utenlandsk.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.log
import no.nav.syfo.utenlandsk.model.UtenlandskSykmeldingNavNoRequest
import no.nav.syfo.utenlandsk.model.UtenlandskSykmeldingRequest
import no.nav.syfo.utenlandsk.opprettJournalpostservice.UtenlandskSykmeldingService

fun Route.registrerUtenlandskPapirsykmeldingApi(utenlandskSykeldingService: UtenlandskSykmeldingService) {
    post("/utenlands/opprett") {
        val request = call.receive<UtenlandskSykmeldingRequest>()
        try {
            if (request.antallPdfs > 10) {
                call.respond(HttpStatusCode.BadRequest, "antallPdfs cannot be > 10")
            } else {
                utenlandskSykeldingService.opprettUtenlanskPdf(request)
                call.respond(HttpStatusCode.OK)
            }
        } catch (e: Exception) {
            log.error("Exception", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
    post("/utenlands/nav/opprett") {
        val request = call.receive<UtenlandskSykmeldingNavNoRequest>()
        try {
            utenlandskSykeldingService.opprettUtenlanskNavNo(request)
            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            log.error("Exception", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
