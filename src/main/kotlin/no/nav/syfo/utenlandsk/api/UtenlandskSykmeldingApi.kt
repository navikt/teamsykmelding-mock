package no.nav.syfo.utenlandsk.api

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import no.nav.syfo.utenlandsk.model.UtenlandskSykmeldingRequest
import no.nav.syfo.utenlandsk.service.UtenlandskSykmeldingService


fun Route.registrerPapirsykmeldingApi(utenlandskSykeldingService: UtenlandskSykmeldingService) {
    post("/utenlands/opprett") {
        val request = call.receive<UtenlandskSykmeldingRequest>()


        // ka e det me ska????????????????????
        // rinaskjetta
    }
}
