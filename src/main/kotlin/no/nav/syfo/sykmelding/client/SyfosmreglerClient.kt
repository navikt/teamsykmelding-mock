package no.nav.syfo.sykmelding.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.io.IOException
import no.nav.syfo.azuread.AccessTokenClient
import no.nav.syfo.log
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.ValidationResult

class SyfosmreglerClient(
    private val syfosmreglerUrl: String,
    private val accessTokenClient: AccessTokenClient,
    private val syfosmreglerScope: String,
    private val httpClient: HttpClient,
) {
    suspend fun sjekkRegler(receivedSykmelding: ReceivedSykmelding): ValidationResult {
        val accessToken = accessTokenClient.getAccessToken(syfosmreglerScope)
        val httpResponse =
            httpClient.post("$syfosmreglerUrl/v1/rules/validate") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                setBody(receivedSykmelding)
            }
        if (httpResponse.status == HttpStatusCode.OK) {
            return httpResponse.body<ValidationResult>()
        } else {
            log.error(
                "Syfosmregler svarte med feilkode ${httpResponse.status} for ${receivedSykmelding.sykmelding.msgId}"
            )
            throw IOException("Syfosmregler svarte med feilkode ${httpResponse.status}")
        }
    }
}
