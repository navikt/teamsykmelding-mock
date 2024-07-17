import io.ktor.server.application.*
import no.nav.syfo.plugins.ApplicationState
import no.nav.syfo.utils.EnvironmentVariables
import org.koin.core.KoinApplication
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        initPorductionModules()
    }
}

fun KoinApplication.initPorductionModules() {
    modules(
        environmentModule,
        applicationStateModule,
    )
}

val environmentModule = module { single { EnvironmentVariables() } }

val applicationStateModule = module { single { ApplicationState() } }
