package no.nav.syfo.papirsykmelding.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.NotFound
import java.io.IOException
import java.util.UUID
import no.nav.syfo.azuread.AccessTokenClientV2
import no.nav.syfo.logger

interface NorskHelsenettClient {
    suspend fun finnBehandlerFnr(hprNummer: String): String?
}

class NorskHelsenettClientProduction(
    private val norskHelsenettUrl: String,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val norskHelsenettScope: String,
    private val httpClient: HttpClient,
) : NorskHelsenettClient {

    override suspend fun finnBehandlerFnr(hprNummer: String): String? {
        val httpResponse: HttpResponse =
            httpClient.get("$norskHelsenettUrl/api/v2/behandlerMedHprNummer") {
                accept(ContentType.Application.Json)
                val accessToken = accessTokenClientV2.getAccessTokenV2(norskHelsenettScope)
                headers {
                    append("Authorization", "Bearer $accessToken")
                    append("Nav-CallId", UUID.randomUUID().toString())
                    append("hprNummer", hprNummer)
                }
            }
        return when (httpResponse.status) {
            HttpStatusCode.InternalServerError -> {
                logger.error("Syfohelsenettproxy svarte med feilmelding")
                throw IOException("Syfohelsenettproxy svarte med feilmelding")
            }
            NotFound -> {
                logger.warn("Fant ikke behandler for HprNummer $hprNummer")
                null
            }
            else -> {
                httpResponse.call.response.body<Behandler>().fnr
            }
        }
    }
}

data class Behandler(
    val fnr: String?,
)
