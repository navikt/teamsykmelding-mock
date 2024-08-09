package no.nav.syfo.azuread

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.*
import io.ktor.http.*
import java.time.Instant
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nav.syfo.utils.logger

interface AccessTokenClientV2 {
    suspend fun getAccessTokenV2(resource: String): String
}

class DevelopmentAccessTokenClientV2 : AccessTokenClientV2 {
    override suspend fun getAccessTokenV2(resource: String): String {
        return "token"
    }
}

class ProductionAccessTokenClientV2(
    private val aadAccessTokenUrl: String,
    private val clientId: String,
    private val clientSecret: String,
    private val httpClient: HttpClient,
) : AccessTokenClientV2 {
    private val mutex = Mutex()

    @Volatile private var tokenMap = HashMap<String, AadAccessTokenMedExpiry>()

    override suspend fun getAccessTokenV2(resource: String): String {
        val omToMinutter = Instant.now().plusSeconds(120L)
        return mutex.withLock {
            (tokenMap[resource]?.takeUnless { it.expiresOn.isBefore(omToMinutter) }
                    ?: run {
                        logger.debug("Henter nytt token fra Azure AD")
                        val tokenExchangeResponse =
                            httpClient.post(aadAccessTokenUrl) {
                                accept(ContentType.Application.Json)
                                method = HttpMethod.Post
                                setBody(
                                    FormDataContent(
                                        Parameters.build {
                                            append("client_id", clientId)
                                            append("scope", resource)
                                            append("grant_type", "client_credentials")
                                            append("client_secret", clientSecret)
                                        },
                                    ),
                                )
                            }

                        if (tokenExchangeResponse.status != HttpStatusCode.OK) {
                            throw IllegalStateException(
                                "Feil ved henting av token fra Azure AD, ${tokenExchangeResponse.status}, body: ${tokenExchangeResponse.bodyAsText()}"
                            )
                        }

                        val body: AadAccessTokenV2 = tokenExchangeResponse.body()
                        val tokenMedExpiry =
                            AadAccessTokenMedExpiry(
                                access_token = body.access_token,
                                expires_in = body.expires_in,
                                expiresOn = Instant.now().plusSeconds(body.expires_in.toLong()),
                            )
                        tokenMap[resource] = tokenMedExpiry

                        logger.debug("Har hentet accesstoken")
                        return@run tokenMedExpiry
                    })
                .access_token
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AadAccessTokenV2(
    val access_token: String,
    val expires_in: Int,
)

data class AadAccessTokenMedExpiry(
    val access_token: String,
    val expires_in: Int,
    val expiresOn: Instant,
)
