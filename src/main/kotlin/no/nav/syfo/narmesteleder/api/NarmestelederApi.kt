package no.nav.syfo.narmesteleder.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import no.nav.syfo.log
import no.nav.syfo.narmesteleder.NarmestelederService
import java.time.LocalDate

fun Route.registrerNarmestelederApi(narmestelederService: NarmestelederService) {
    post("/narmesteleder/opprett") {
        val request = call.receive<OpprettNarmestelederRequest>()

        if (request.ansattFnr.trim { it <= ' ' }.length == 11 && request.lederFnr.trim { it <= ' ' }.length == 11 &&
            request.orgnummer.trim { it <= ' ' }.length == 9
        ) {
            narmestelederService.registrerNarmesteleder(
                request.copy(
                    ansattFnr = request.ansattFnr.trim { it <= ' ' },
                    lederFnr = request.lederFnr.trim { it <= ' ' },
                    orgnummer = request.orgnummer.trim { it <= ' ' }
                )
            )
            log.info("Opprettet nærmesteleder-kobling")
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.BadRequest, "Feil lengde på fødselsnummer eller orgnummer")
        }
    }
}

data class OpprettNarmestelederRequest(
    val ansattFnr: String,
    val lederFnr: String,
    val orgnummer: String,
    val mobil: String,
    val epost: String,
    val forskutterer: Boolean,
    val aktivFom: LocalDate
)
