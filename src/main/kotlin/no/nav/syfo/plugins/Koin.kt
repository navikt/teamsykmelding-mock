import io.ktor.server.application.*
import no.nav.syfo.azuread.AccessTokenClientV2
import no.nav.syfo.azuread.ProductionAccessTokenClientV2
import no.nav.syfo.clients.createHttpClient
import no.nav.syfo.legeerklaering.LegeerklaeringService
import no.nav.syfo.mq.MqClient
import no.nav.syfo.mq.MqClientProduction
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.narmesteleder.kafka.NlResponseProducer
import no.nav.syfo.narmesteleder.kafka.NlResponseProducerProduction
import no.nav.syfo.oppgave.OppgaveClient
import no.nav.syfo.oppgave.OppgaveClientProduction
import no.nav.syfo.papirsykmelding.PapirsykmeldingService
import no.nav.syfo.papirsykmelding.client.DokarkivClient
import no.nav.syfo.papirsykmelding.client.DokarkivClientProduction
import no.nav.syfo.papirsykmelding.client.NorskHelsenettClient
import no.nav.syfo.papirsykmelding.client.NorskHelsenettClientProduction
import no.nav.syfo.papirsykmelding.client.SyfosmpapirreglerClient
import no.nav.syfo.papirsykmelding.client.SyfosmpapirreglerClientProduction
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.ProductionPdlClient
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.pdl.service.PdlPersonServiceProduction
import no.nav.syfo.plugins.ApplicationState
import no.nav.syfo.plugins.getProductionAuthConfig
import no.nav.syfo.sykmelding.SlettSykmeldingService
import no.nav.syfo.sykmelding.SykmeldingService
import no.nav.syfo.sykmelding.client.SyfosmregisterClient
import no.nav.syfo.sykmelding.client.SyfosmregisterClientProduction
import no.nav.syfo.sykmelding.client.SyfosmreglerClient
import no.nav.syfo.sykmelding.client.SyfosmreglerClientProduction
import no.nav.syfo.sykmelding.kafka.SykmeldingStatusKafkaProducer
import no.nav.syfo.sykmelding.kafka.SykmeldingStatusKafkaProducerProduction
import no.nav.syfo.sykmelding.kafka.TombstoneKafkaProducer
import no.nav.syfo.sykmelding.kafka.TombstoneKafkaProducerProduction
import no.nav.syfo.utenlandsk.service.UtenlandskSykmeldingService
import no.nav.syfo.utils.EnvironmentVariables
import no.nav.syfo.utils.ServiceUser
import org.koin.core.KoinApplication
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        initProductionModules()
        if (environment.developmentMode) {
            initDevelopmentModules()
        }
    }
}

fun KoinApplication.initProductionModules() {
    modules(
        environmentModule,
        applicationStateModule,
        authModule,
        httpClientModule,
        pdlModule,
        kafkaModules,
        oppgaveModule,
        dokarkivModule,
        mqModule,
        syfosmregisterModule,
        syfosmreglerModule,
        syfosmpapirreglerModule,
        norskhelsenettModule,
        narmestelederModule,
        sykmeldingModule,
        legeerklaeringModule,
    )
}

val environmentModule = module { single { EnvironmentVariables() } }

val applicationStateModule = module { single { ApplicationState() } }

val authModule = module { single { getProductionAuthConfig(get()) } }

val httpClientModule = module {
    single { createHttpClient() }
    single<AccessTokenClientV2> {
        val env = get<EnvironmentVariables>()

        ProductionAccessTokenClientV2(
            aadAccessTokenUrl = env.aadAccessTokenUrl,
            clientId = env.clientId,
            clientSecret = env.clientSecret,
            httpClient = get(),
        )
    }
}

val pdlModule = module {
    single<PdlClient> {
        ProductionPdlClient(
            httpClient = get(),
            basePath = get<EnvironmentVariables>().pdlGraphqlPath,
            graphQlQuery =
                PdlClient::class
                    .java
                    .getResource("/graphql/getPerson.graphql")!!
                    .readText()
                    .replace(Regex("[\n\t]"), ""),
        )
    }
    single<PdlPersonService> {
        PdlPersonServiceProduction(get(), get(), get<EnvironmentVariables>().pdlScope)
    }
}

val kafkaModules = module {
    single<NlResponseProducer> {
        NlResponseProducerProduction(
            get<EnvironmentVariables>().narmestelederTopic,
        )
    }
    single<TombstoneKafkaProducer> {
        TombstoneKafkaProducerProduction(
            listOf(
                get<EnvironmentVariables>().papirSmRegistreringTopic,
                get<EnvironmentVariables>().manuellTopic,
            ),
        )
    }

    single<SykmeldingStatusKafkaProducer> {
        SykmeldingStatusKafkaProducerProduction(
            get<EnvironmentVariables>().sykmeldingStatusTopic,
        )
    }
}

val oppgaveModule = module {
    single<OppgaveClient> {
        OppgaveClientProduction(
            url = get<EnvironmentVariables>().oppgaveUrl,
            accessTokenClientV2 = get(),
            scope = get<EnvironmentVariables>().oppgaveScope,
            httpClient = get(),
        )
    }
}

val dokarkivModule = module {
    single<DokarkivClient> {
        val env = get<EnvironmentVariables>()
        DokarkivClientProduction(
            url = env.dokarkivUrl,
            accessTokenClientV2 = get(),
            scope = env.dokarkivScope,
            httpClient = get(),
        )
    }
}

val mqModule = module {
    single<MqClient> {
        val env = get<EnvironmentVariables>()

        MqClientProduction(
            env = env,
            serviceUser = ServiceUser(),
        )
    }
}

val syfosmregisterModule = module {
    single<SyfosmregisterClient> {
        val env = get<EnvironmentVariables>()
        SyfosmregisterClientProduction(
            syfosmregisterUrl = env.syfosmregisterUrl,
            accessTokenClientV2 = get(),
            syfosmregisterScope = env.syfosmregisterScope,
            httpClient = get(),
        )
    }
}

val syfosmreglerModule = module {
    single<SyfosmreglerClient> {
        val env = get<EnvironmentVariables>()
        SyfosmreglerClientProduction(
            syfosmreglerUrl = env.syfosmreglerUrl,
            accessTokenClientV2 = get(),
            syfosmreglerScope = env.syfosmreglerScope,
            httpClient = get(),
        )
    }
}

val syfosmpapirreglerModule = module {
    single<SyfosmpapirreglerClient> {
        val env = get<EnvironmentVariables>()
        SyfosmpapirreglerClientProduction(
            syfosmpapirreglerUrl = env.syfosmpapirreglerUrl,
            accessTokenClientV2 = get(),
            syfosmpapirreglerScope = env.syfosmpapirreglerScope,
            httpClient = get(),
        )
    }
}

val norskhelsenettModule = module {
    single<NorskHelsenettClient> {
        val env = get<EnvironmentVariables>()
        NorskHelsenettClientProduction(
            norskHelsenettUrl = env.norskHelsenettUrl,
            accessTokenClientV2 = get(),
            norskHelsenettScope = env.norskHelsenettScope,
            httpClient = get(),
        )
    }
}

val narmestelederModule = module {
    single {
        NarmestelederService(
            nlResponseProducer = get(),
            pdlPersonService = get(),
        )
    }
}

val legeerklaeringModule = module {
    single {
        val env = get<EnvironmentVariables>()
        LegeerklaeringService(
            pdlPersonService = get(),
            mqClient = get(),
            legeerklaeringQueue = env.legeerklaeringQueue
        )
    }
}

val sykmeldingModule = module {
    single {
        SlettSykmeldingService(
            syfosmregisterClient = get(),
            sykmeldingStatusKafkaProducer = get(),
            tombstoneKafkaProducer = get(),
        )
    }
    single {
        val env = get<EnvironmentVariables>()
        SykmeldingService(
            pdlPersonService = get(),
            mqClient = get(),
            sykmeldingQueue = env.sykmeldingQueue,
            syfosmreglerClient = get()
        )
    }

    single {
        PapirsykmeldingService(
            dokarkivClient = get(),
            syfosmpapirreglerClient = get(),
            norskHelsenettClient = get()
        )
    }

    single { UtenlandskSykmeldingService(dokarkivClient = get(), oppgaveClient = get()) }
}
