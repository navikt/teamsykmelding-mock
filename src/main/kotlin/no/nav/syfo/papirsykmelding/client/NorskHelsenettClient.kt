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
import no.nav.syfo.azuread.AccessTokenClient
import no.nav.syfo.log
import java.io.IOException
import java.util.UUID

class NorskHelsenettClient(
    private val norskHelsenettUrl: String,
    private val accessTokenClient: AccessTokenClient,
    private val norskHelsenettScope: String,
    private val httpClient: HttpClient
) {

    suspend fun finnBehandlerFnr(hprNummer: String): String? {
        val httpResponse: HttpResponse = httpClient.get("$norskHelsenettUrl/api/v2/behandlerMedHprNummer") {
            accept(ContentType.Application.Json)
            val accessToken = accessTokenClient.getAccessToken(norskHelsenettScope)
            headers {
                append("Authorization", "Bearer $accessToken")
                append("Nav-CallId", UUID.randomUUID().toString())
                append("hprNummer", hprNummer)
            }
        }
        return when (httpResponse.status) {
            HttpStatusCode.InternalServerError -> {
                log.error("Syfohelsenettproxy svarte med feilmelding")
                throw IOException("Syfohelsenettproxy svarte med feilmelding")
            }
            NotFound -> {
                log.warn("Fant ikke behandler for HprNummer $hprNummer")
                null
            }
            else -> {
                httpResponse.call.response.body<Behandler>().fnr
            }
        }
    }
}

data class Behandler(
    val fnr: String?
)
