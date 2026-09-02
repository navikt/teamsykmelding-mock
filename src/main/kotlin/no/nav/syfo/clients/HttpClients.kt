package no.nav.syfo.clients

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.engine.apache5.Apache5EngineConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.network.sockets.SocketTimeoutException
import io.ktor.serialization.jackson3.jackson

fun createHttpClient(): HttpClient {
    val config: HttpClientConfig<Apache5EngineConfig>.() -> Unit = {
        install(HttpTimeout) {
            connectTimeoutMillis = 10000
            requestTimeoutMillis = 10000
            socketTimeoutMillis = 10000
        }
        install(ContentNegotiation) {
            jackson {
                // setSerializationInclusion(JsonInclude.Include.NON_NULL)
            }
        }
        expectSuccess = false
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                when (exception) {
                    is SocketTimeoutException ->
                        throw ServiceUnavailableException(exception.message)
                }
            }
        }
    }

    return HttpClient(Apache5, config)
}

class ServiceUnavailableException(message: String?) : Exception(message)
