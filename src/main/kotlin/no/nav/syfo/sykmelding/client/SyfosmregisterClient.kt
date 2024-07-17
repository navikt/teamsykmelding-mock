package no.nav.syfo.sykmelding.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import no.nav.syfo.azuread.AccessTokenClientV2

interface SyfosmregisterClient {
    suspend fun hentSykmeldinger(fnr: String): List<Sykmelding>
}

class SyfosmregisterClientProduction(
    private val syfosmregisterUrl: String,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val syfosmregisterScope: String,
    private val httpClient: HttpClient,
) : SyfosmregisterClient {
    override suspend fun hentSykmeldinger(fnr: String): List<Sykmelding> =
        httpClient
            .get("$syfosmregisterUrl/api/v2/sykmelding/sykmeldinger") {
                accept(ContentType.Application.Json)
                val accessToken = accessTokenClientV2.getAccessTokenV2(syfosmregisterScope)
                headers {
                    append("Authorization", "Bearer $accessToken")
                    append("fnr", fnr)
                }
            }
            .body()
}

class SyfosmregisterClientDevelopment() : SyfosmregisterClient {
    override suspend fun hentSykmeldinger(fnr: String): List<Sykmelding> {
        return listOf(Sykmelding("1"))
    }
}

data class Sykmelding(
    val id: String,
)
