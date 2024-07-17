package no.nav.syfo.routes.nais.isready

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import no.nav.syfo.plugins.ApplicationState

fun Route.naisIsReadyRoute(
    applicationState: ApplicationState,
    readynessCheck: () -> Boolean = { applicationState.ready },
) {
    get("/is_ready") {
        if (readynessCheck()) {
            call.respondText("I'm ready! :)")
        } else {
            call.respondText(
                "Please wait! I'm not ready :(",
                status = HttpStatusCode.InternalServerError
            )
        }
    }
}
