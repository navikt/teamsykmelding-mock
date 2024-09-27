package no.nav.syfo.plugins

import io.ktor.server.application.*
import no.nav.syfo.mq.MqClient
import org.koin.ktor.ext.inject

fun Application.configureLifecycleHooks() {
    val state by inject<ApplicationState>()
    val mqClient by inject<MqClient>()

    environment.monitor.subscribe(ApplicationStarted) {
        state.ready = true
        mqClient.connection?.start()
    }
    environment.monitor.subscribe(ApplicationStopped) {
        state.ready = false
        mqClient.connection?.close()
    }
}

data class ApplicationState(
    var alive: Boolean = true,
    var ready: Boolean = true,
)
