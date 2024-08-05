package no.nav.syfo.mq

import javax.jms.Connection
import no.nav.syfo.logger
import no.nav.syfo.utils.EnvironmentVariables
import no.nav.syfo.utils.ServiceUser

interface MqClient {
    fun getConnection(): Connection?
}

class MqClientProduction(
    private val env: EnvironmentVariables,
    private val serviceUser: ServiceUser,
) : MqClient {

    override fun getConnection(): Connection? {
        val connection =
            connectionFactory(env)
                .apply {
                    sslSocketFactory = null
                    sslCipherSuite = null
                }
                .createConnection(serviceUser.username, serviceUser.password)
        connection.start()
        return connection
    }
}

class MqClientDevelopment() : MqClient {

    override fun getConnection(): Connection? {
        logger.info("Dev??")
        return null
    }
}
