package no.nav.syfo.oppgave

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.registerOppgaveCheckApi() {
    val oppgaveService: OppgaveClient by inject()
    get("/oppgave/{journalpostId}") {
        val journalpostId =
            call.parameters["journalpostId"]
                ?: throw IllegalArgumentException("Missing journalpostId")
        val oppgaveResponse = oppgaveService.getOppgaveId(journalpostId)
        call.respond(oppgaveResponse)
    }
}
