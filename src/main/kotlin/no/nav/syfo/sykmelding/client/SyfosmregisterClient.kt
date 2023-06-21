package no.nav.syfo.sykmelding.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import no.nav.syfo.azuread.AccessTokenClient

class SyfosmregisterClient(
    private val syfosmregisterUrl: String,
    private val accessTokenClient: AccessTokenClient,
    private val syfosmregisterScope: String,
    private val httpClient: HttpClient,
) {
    suspend fun hentSykmeldinger(fnr: String): List<Sykmelding> =
        httpClient
            .get("$syfosmregisterUrl/api/v2/sykmelding/sykmeldinger") {
                accept(ContentType.Application.Json)
                val accessToken = accessTokenClient.getAccessToken(syfosmregisterScope)
                headers {
                    append("Authorization", "Bearer $accessToken")
                    append("fnr", fnr)
                }
            }
            .body()
}

data class Sykmelding(
    val id: String,
)
