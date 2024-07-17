package no.nav.syfo.plugins

import initPorductionModules
import no.nav.syfo.utils.EnvironmentVariables
import org.junit.jupiter.api.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class CheckModulesTest : KoinTest {

    val testEnv =
        EnvironmentVariables(
            applicationPort = 69,
            applicationName = "applicationName-test",
            oppgaveScope = "oppgaveScope-test",
            pdlGraphqlPath = "pdlGraphqlPath-test",
            pdlScope = "pdlScope-test",
            clusterName = "clusterName-test",
            narmestelederTopic = "",
            aadAccessTokenUrl = "",
            clientId = "",
            clientSecret = "",
            mqHostname = "",
            mqPort = 1,
            mqGatewayName = "",
            mqChannelName = "",
            sykmeldingQueue = "",
            legeerklaeringQueue = "",
            dokarkivUrl = "",
            dokarkivScope = "",
            papirSmRegistreringTopic = "",
            manuellTopic = "",
            sykmeldingStatusTopic = "",
            syfosmregisterUrl = "",
            syfosmregisterScope = "",
            syfosmreglerUrl = "",
            syfosmreglerScope = "",
            syfosmpapirreglerUrl = "",
            syfosmpapirreglerScope = "",
            norskHelsenettUrl = "",
            norskHelsenettScope = "",
            oppgaveUrl = "",
        )

    @Test
    fun verifyKoinApp() {
        koinApplication {
            initPorductionModules()

            modules(
                module {
                    // Mock up any "leaf nodes" in the dependency tree that we don't want
                    // instantiated. That way we can verify that all dependencies are satisfied
                    single { testEnv }
                },
            )
            checkModules()
        }
    }
}
