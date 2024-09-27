package no.nav.syfo.mq

import jakarta.jms.Connection
import no.nav.syfo.utils.EnvironmentVariables
import no.nav.syfo.utils.ServiceUser

interface MqClient {
    val connection: Connection?
}

class MqClientProduction(
    env: EnvironmentVariables,
    serviceUser: ServiceUser,
) : MqClient {
    override val connection: Connection? =
        connectionFactory(env).createConnection(serviceUser.username, serviceUser.password)
}

class MqClientDevelopment() : MqClient {
    override val connection: Connection? = null
}
