package no.nav.syfo.papirsykmelding.api

import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.oppgave.OppgaveClient
import no.nav.syfo.oppgave.OppgaveResponse
import no.nav.syfo.papirsykmelding.PapirsykmeldingService
import no.nav.syfo.papirsykmelding.model.PapirsykmeldingMappingException
import no.nav.syfo.papirsykmelding.model.PapirsykmeldingRequest
import no.nav.syfo.papirsykmelding.model.UtenlandskPapirsykmeldingRequest
import no.nav.syfo.utils.logger
import org.koin.ktor.ext.inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun Route.registrerPapirsykmeldingApi() {
    val papirsykmeldingService by inject<PapirsykmeldingService>()
    val oppgaveService: OppgaveClient by inject()
    post("/papirsykmelding/opprett") {
        val request = call.receive<PapirsykmeldingRequest>()

        val journalpostId = papirsykmeldingService.opprettPapirsykmelding(request)

        logger.info("Opprettet papirsykmelding med journalpostId $journalpostId")
        call.respond(
            HttpStatusCode.OK,
            HttpMessage("Opprettet papirsykmelding med journalpostId $journalpostId")
        )
    }

    post("/papirsykmelding/regelsjekk") {
        val request = call.receive<PapirsykmeldingRequest>()
        if (request.utenOcr) {
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage("Kan ikke sjekke papirsykmelding uten OCR")
            )
            return@post
        }

        try {
            val validationResult = papirsykmeldingService.sjekkRegler(request)

            logger.info("Har sjekket regler for papirsykmelding")
            call.respond(validationResult)
        } catch (e: PapirsykmeldingMappingException) {
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage(e.message ?: "Kunne ikke mappe sykmelding til ReceivedSykmelding")
            )
        }
    }

    post("/papirsykmelding/utenlandsk/opprett") {
        val fnrSykmeldt = call.request.headers["Sykmeldt-Fnr"]
        if (fnrSykmeldt != null && fnrSykmeldt.length != 11) {
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage("Sykmeldt-Fnr har feil lengde, er ${fnrSykmeldt.length}")
            )
            return@post
        }
        logger.info("utenlandsk papirsykmelding med fnr fra header: $fnrSykmeldt")
        val request = call.receive<UtenlandskPapirsykmeldingRequest>()
        logger.info("utenlandsk papirsykmelding med fnr fra requestclas: ${request.fnr}")
        val journalpostId = papirsykmeldingService.opprettUtenlandskPapirsykmelding(request.fnr)
        val oppgaver = tryGetOppgaveId(journalpostId, oppgaveService)
        logger.info("Opprettet utenlandsk papirsykmelding med journalpostId $journalpostId")
        call.respond(
            HttpStatusCode.OK,
            HttpMessage("Opprettet utenlandsk papirsykmelding med journalpostId $journalpostId, oppgaveId ${oppgaver.oppgaver.map { it.id }.joinToString()}")
        )
    }
}


suspend fun tryGetOppgaveId(journalpostID: String, oppgaveClient: OppgaveClient): OppgaveResponse {
    var oppgaveResponse = OppgaveResponse(0, emptyList())
    var oppgaveCreated = false
    try {
        withTimeout(15.seconds) {
            while (!oppgaveCreated) {
                oppgaveResponse = oppgaveClient.getOppgaveId(journalpostID)
                if (oppgaveResponse.antallTreffTotalt > 0) {
                    oppgaveCreated = true
                }
                delay(1000)
            }
        }
        return oppgaveResponse;
    } catch (e: Exception) {
        logger.error("Klarte ikke å hente oppgaveId", e)
        return oppgaveResponse
    }
}
