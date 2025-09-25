package no.nav.syfo.plugins

import initProductionModules
import io.mockk.mockk
import no.nav.syfo.mq.MqClient
import no.nav.syfo.narmesteleder.kafka.NlResponseProducer
import no.nav.syfo.narmesteleder.kafka.NlResponseProducerDevelopment
import no.nav.syfo.papirsykmelding.client.DokarkivClient
import no.nav.syfo.papirsykmelding.client.DokarkivClientDevelopment
import no.nav.syfo.sykmelding.SlettSykmeldingService
import no.nav.syfo.sykmelding.SykmeldingService
import no.nav.syfo.sykmelding.kafka.SykmeldingStatusKafkaProducer
import no.nav.syfo.sykmelding.kafka.SykmeldingStatusKafkaProducerDevelopment
import no.nav.syfo.sykmelding.kafka.TombstoneKafkaProducer
import no.nav.syfo.sykmelding.kafka.TombstoneKafkaProducerDevelopment
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
            jwkKeysUrl = "",
            jwtIssuer = "",
            inputDolly = "",
        )

    @Test
    fun verifyKoinApp() {
        koinApplication {
            initProductionModules()

            modules(
                module {
                    // Mock up any "leaf nodes" in the dependency tree that we don't want
                    // instantiated. That way we can verify that all dependencies are satisfied
                    single { testEnv }
                    single<TombstoneKafkaProducer>() { mockk<TombstoneKafkaProducerDevelopment>() }
                    single<DokarkivClient>() { mockk<DokarkivClientDevelopment>() }
                    single<SykmeldingStatusKafkaProducer>() {
                        mockk<SykmeldingStatusKafkaProducerDevelopment>()
                    }
                    single<SlettSykmeldingService>() { mockk<SlettSykmeldingService>() }
                    single<SykmeldingService>() { mockk<SykmeldingService>() }
                    single<MqClient>() { mockk<MqClient>() }
                    single<NlResponseProducer>() { mockk<NlResponseProducerDevelopment>() }
                },
            )

            // TODO: Replace deprecated API
            checkModules()
        }
    }
}
