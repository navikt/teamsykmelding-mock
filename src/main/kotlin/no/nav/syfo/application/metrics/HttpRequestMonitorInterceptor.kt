package no.nav.syfo.application.metrics

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.path
import io.ktor.util.pipeline.PipelineContext

val REGEX = """[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}""".toRegex()

fun monitorHttpRequests(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit {
    return {
        val path = context.request.path()
        val label = REGEX.replace(path, ":id")
        val timer = HTTP_HISTOGRAM.labels(label).startTimer()
        proceed()
        timer.observeDuration()
    }
}
