package no.nav.syfo.utenlandsk.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.application.HttpMessage
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
                val journalpostId = utenlandskSykeldingService.opprettUtenlanskPdf(request)
                call.respond(HttpStatusCode.OK, HttpMessage("Opprettet utenlandsk papirsykmelding med journalpostId $journalpostId"))
            }
        } catch (exception: Exception) {
            log.error("Exception", exception)
            call.respond(HttpStatusCode.InternalServerError, HttpMessage(exception.message ?: "Unknown error"))
        }
    }
    post("/utenlands/nav/opprett") {
        val request = call.receive<UtenlandskSykmeldingNavNoRequest>()
        try {
            val journalpostId = utenlandskSykeldingService.opprettUtenlanskNavNo(request)
            call.respond(HttpStatusCode.OK, HttpMessage("Opprettet utenlandsk papirsykmelding fra nav.no med journalpostId $journalpostId"))
        } catch (exception: Exception) {
            log.error("Exception", exception)
            call.respond(HttpStatusCode.InternalServerError, HttpMessage(exception.message ?: "Unknown error"))
        }
    }
}
