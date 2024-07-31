package no.nav.syfo.metrics

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.pipeline.PipelineContext
import no.nav.syfo.utils.logger

fun monitorHttpRequests(
    developmentMode: Boolean
): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit {
    return {
        try {
            if (
                !(call.request.uri.contains("assets") ||
                    call.request.uri.contains("internal") ||
                    call.request.uri.contains("favicon"))
            ) {
                logger.info("Received request: ${call.request.uri}")
            }
            val label = context.request.path()
            val timer = HTTP_HISTOGRAM.labels(label).startTimer()
            proceed()
            timer.observeDuration()
        } catch (e: Exception) {
            if (developmentMode) {
                logger.error(
                    "Exception during '${call.request.uri}': ${e.javaClass.simpleName}: ${e.message}",
                    e,
                )
            } else {
                logger.error(
                    "Feil under behandling av HTTP-forespørsel til '${call.request.uri}': ${e.javaClass.simpleName}"
                )
            }
            logger.error(
                "Feil under behandling av HTTP-forespørsel til '${call.request.uri}': ${e.javaClass.simpleName}: ${e.message}. Se exception for detaljer.",
                e,
            )
            throw e
        }
    }
}
