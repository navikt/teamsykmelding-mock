package no.nav.syfo.utenlandsk.api

import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.model.OppgaveOpprettetResponse
import no.nav.syfo.utenlandsk.model.UtenlandskSykmeldingNavNoRequest
import no.nav.syfo.utenlandsk.model.UtenlandskSykmeldingPdfRequest
import no.nav.syfo.utenlandsk.service.UtenlandskSykmeldingService
import no.nav.syfo.utils.logger
import org.koin.ktor.ext.inject

fun Route.registrerUtenlandskPapirsykmeldingApi() {
    val utenlandskSykeldingService by inject<UtenlandskSykmeldingService>()

    post("/utenlands/opprett") {
        val request = call.receive<UtenlandskSykmeldingPdfRequest>()
        try {
            if (request.antallPdfs > 10) {
                call.respond(HttpStatusCode.BadRequest, "antallPdfs cannot be > 10")
            } else {
                val oppgaveId = utenlandskSykeldingService.opprettUtenlanskPdf(request)
                call.respond(
                    HttpStatusCode.OK,
                    OppgaveOpprettetResponse(
                        "OK",
                        "Opprettet utenlandsk papirsykmelding med oppgaveId $oppgaveId",
                        oppgaveId
                    )
                )
            }
        } catch (exception: Exception) {
            logger.error("Exception", exception)
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage(exception.message ?: "Unknown error")
            )
        }
    }
    post("/utenlands/nav/opprett") {
        val request = call.receive<UtenlandskSykmeldingNavNoRequest>()
        try {
            val oppgaveId = utenlandskSykeldingService.opprettUtenlanskNavNo(request)
            call.respond(
                HttpStatusCode.OK,
                OppgaveOpprettetResponse(
                    "OK",
                    "Opprettet utenlandsk papirsykmelding med oppgaveId $oppgaveId",
                    oppgaveId
                )
            )
        } catch (exception: Exception) {
            logger.error("Exception", exception)
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage(exception.message ?: "Unknown error")
            )
        }
    }
}
