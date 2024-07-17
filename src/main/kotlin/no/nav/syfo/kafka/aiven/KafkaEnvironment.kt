package no.nav.syfo.kafka.aiven

data class KafkaEnvironment(
    val KAFKA_BROKERS: String = getEnvVar("KAFKA_BROKERS"),
    val KAFKA_CLIENT_ID: String = getEnvVar("HOSTNAME"),
    val KAFKA_TRUSTSTORE_PATH: String = getEnvVar("KAFKA_TRUSTSTORE_PATH"),
    val KAFKA_KEYSTORE_PATH: String = getEnvVar("KAFKA_KEYSTORE_PATH"),
    val KAFKA_CREDSTORE_PASSWORD: String = getEnvVar("KAFKA_CREDSTORE_PASSWORD")
) {
    companion object {
        fun getEnvVar(varName: String, defaultValue: String? = null) =
            System.getenv(varName)
                ?: defaultValue
                ?: throw RuntimeException("Missing required variable \"$varName\"")
    }
}
