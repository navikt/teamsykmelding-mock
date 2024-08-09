package no.nav.syfo.mq

data class MQEnvironment(
    val MQ_KEYSTORE_PASSWORD: String = getEnvVar("MQ_KEYSTORE_PASSWORD"),
    val MQ_KEYSTORE_PATH: String = getEnvVar("MQ_KEYSTORE_PATH")
) {
    companion object {
        fun getEnvVar(varName: String, defaultValue: String? = null) =
            System.getenv(varName)
                ?: defaultValue
                ?: throw RuntimeException("Missing required variable \"$varName\"")
    }
}
