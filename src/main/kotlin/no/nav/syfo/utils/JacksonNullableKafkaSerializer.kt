package no.nav.syfo.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.common.serialization.Serializer

class JacksonNullableKafkaSerializer<T : Any> : Serializer<T> {

    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        objectMapper.apply {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }

    override fun serialize(topic: String?, data: T?): ByteArray? {
        return when (data) {
            null -> null
            else -> objectMapper.writeValueAsBytes(data)
        }
    }

    override fun close() {}
}
