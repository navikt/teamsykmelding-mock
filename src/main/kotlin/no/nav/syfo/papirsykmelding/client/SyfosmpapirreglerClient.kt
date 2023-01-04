package no.nav.syfo.papirsykmelding.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.syfo.azuread.AccessTokenClient
import no.nav.syfo.log
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.ValidationResult
import java.io.IOException

class SyfosmpapirreglerClient(
    private val syfosmpapirreglerUrl: String,
    private val accessTokenClient: AccessTokenClient,
    private val syfosmpapirreglerScope: String,
    private val httpClient: HttpClient
) {
    suspend fun sjekkRegler(receivedSykmelding: ReceivedSykmelding): ValidationResult {
        val accessToken = accessTokenClient.getAccessToken(syfosmpapirreglerScope)
        val httpResponse = httpClient.post("$syfosmpapirreglerUrl/api/v2/rules/validate") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("Authorization", "Bearer $accessToken")
            setBody(receivedSykmelding)
        }
        if (httpResponse.status == HttpStatusCode.OK) {
            return httpResponse.body<ValidationResult>()
        } else {
            log.error("Syfosmpapirregler svarte med feilkode ${httpResponse.status} for ${receivedSykmelding.sykmelding.msgId}")
            throw IOException("Syfosmpapirregler svarte med feilkode ${httpResponse.status}")
        }
    }
}
