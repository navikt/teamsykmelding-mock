package no.nav.syfo

import no.nav.syfo.mq.MqConfig

data class Environment(
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),
    val applicationName: String = getEnvVar("NAIS_APP_NAME", "teamsykmelding-mock-backend"),
    val narmestelederTopic: String = "teamsykmelding.syfo-narmesteleder",
    val pdlScope: String = getEnvVar("PDL_SCOPE"),
    val pdlGraphqlPath: String = getEnvVar("PDL_GRAPHQL_PATH"),
    val aadAccessTokenUrl: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
    val clientId: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val clientSecret: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    override val mqHostname: String = getEnvVar("MQ_HOST_NAME"),
    override val mqPort: Int = getEnvVar("MQ_PORT").toInt(),
    override val mqGatewayName: String = getEnvVar("MQ_GATEWAY_NAME"),
    override val mqChannelName: String = getEnvVar("MQ_CHANNEL_NAME"),
    val sykmeldingQueue: String = "QA.Q1_SYFOSMMOTTAK.INPUT",
    val legeerklaeringQueue: String = "QA.Q1_PALE.INPUT",
    val dokarkivUrl: String = getEnvVar("DOKARKIV_URL"),
    val dokarkivScope: String = getEnvVar("DOKARKIV_SCOPE"),
    val papirSmRegistreringTopic: String = "teamsykmelding.papir-sm-registering",
    val manuellTopic: String = "teamsykmelding.sykmelding-manuell",
    val sykmeldingStatusTopic: String = "teamsykmelding.sykmeldingstatus-leesah",
    val syfosmregisterUrl: String = "http://syfosmregister",
    val syfosmregisterScope: String = getEnvVar("SYFOSMREGISTER_SCOPE"),
    val syfosmreglerUrl: String = "http://syfosmregler",
    val syfosmreglerScope: String = getEnvVar("SYFOSMREGLER_SCOPE"),
    val syfosmpapirreglerUrl: String = "http://syfosmpapirregler",
    val syfosmpapirreglerScope: String = getEnvVar("SYFOSMPAPIRREGLER_SCOPE"),
    val norskHelsenettUrl: String = "http://syfohelsenettproxy",
    val norskHelsenettScope: String = getEnvVar("NORSKHELSENETT_SCOPE"),
    val oppgaveUrl: String = getEnvVar("OPPGAVEBEHANDLING_URL"),
    val oppgaveScope: String = getEnvVar("OPPGAVE_SCOPE"),
    val clusterName: String = getEnvVar("NAIS_CLUSTER_NAME"),
) : MqConfig

data class ServiceUser(
    val username: String = getEnvVar("SERVICEUSER_USERNAME"),
    val password: String = getEnvVar("SERVICEUSER_PASSWORD"),
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName)
        ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
