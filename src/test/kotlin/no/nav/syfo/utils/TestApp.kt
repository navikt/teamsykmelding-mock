package no.nav.syfo.utils

import applicationStateModule
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import no.nav.syfo.plugins.configureContentNegotiation
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

class Blocks {
    var openRouteConfiguration: (Route.() -> Unit)? = null
    var invalidRouteConfiguration: (Route.() -> Unit)? = null

    fun dependencies(koin: KoinAppDeclaration?) {
        startKoin {
            modules(mockedEnvModule, applicationStateModule)
            if (koin != null) {
                koin()
            }
        }
    }

    fun openRoutes(configure: Route.() -> Unit) {
        openRouteConfiguration = configure
    }

    fun invalidRoutes(configure: Route.() -> Unit) {
        invalidRouteConfiguration = configure
    }
}

fun ApplicationTestBuilder.setupTestApplication(
    setup: (Blocks.() -> Unit)? = null,
) {
    val blocks =
        if (setup != null) {
            Blocks().apply(setup)
        } else {
            startKoin { modules(mockedEnvModule, applicationStateModule) }
            null
        }

    application {
        configureContentNegotiation()
        routing { blocks?.openRouteConfiguration?.let { it(this) } }
    }
}

val mockedEnvModule = module {
    single {
        mockk<EnvironmentVariables>(relaxed = true) { every { clientId } returns "client_id" }
    }
}
