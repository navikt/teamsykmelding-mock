package no.nav.syfo.mq

import java.util.Properties

class MqTlsUtils {
    companion object {
        fun getMqTlsConfig(): Properties {
            return Properties().also {
                val mqEnv = MQEnvironment()
                it["javax.net.ssl.keyStore"] = mqEnv.MQ_KEYSTORE_PATH
                it["javax.net.ssl.keyStorePassword"] = mqEnv.MQ_KEYSTORE_PASSWORD
                it["javax.net.ssl.keyStoreType"] = "jks"
            }
        }
    }
}
