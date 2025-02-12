package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import no.nav.syfo.legeerklaering.api.registrerLegeerklaeringApi
import no.nav.syfo.metrics.monitorHttpRequests
import no.nav.syfo.narmesteleder.api.registrerNarmestelederApi
import no.nav.syfo.oppgave.registerOppgaveCheckApi
import no.nav.syfo.papirsykmelding.api.registrerPapirsykmeldingApi
import no.nav.syfo.sykmelding.api.registrerSykmeldingApi
import no.nav.syfo.utenlandsk.api.registrerUtenlandskPapirsykmeldingApi

fun Application.configureFeatures() {
    routing {
        route("/api") {
            registrerSykmeldingApi()
            registrerUtenlandskPapirsykmeldingApi()
            registrerPapirsykmeldingApi()
            registerOppgaveCheckApi()
            registrerNarmestelederApi()
            registrerLegeerklaeringApi()
        }
    }

    intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests(developmentMode))
}
