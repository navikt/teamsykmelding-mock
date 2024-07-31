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
import no.nav.syfo.azuread.AccessTokenClientV2
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.utils.logger

interface SyfosmreglerClient {
    suspend fun sjekkRegler(receivedSykmelding: ReceivedSykmelding): ValidationResult
}

class SyfosmreglerClientProduction(
    private val syfosmreglerUrl: String,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val syfosmreglerScope: String,
    private val httpClient: HttpClient,
) : SyfosmreglerClient {
    override suspend fun sjekkRegler(receivedSykmelding: ReceivedSykmelding): ValidationResult {
        val accessToken = accessTokenClientV2.getAccessTokenV2(syfosmreglerScope)
        logger.info("Gjør kall mot syfosmregler api")
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
            logger.error(
                "Syfosmregler svarte med feilkode ${httpResponse.status} for ${receivedSykmelding.sykmelding.msgId}"
            )
            throw IOException("Syfosmregler svarte med feilkode ${httpResponse.status}")
        }
    }
}

class SyfosmreglerClientDevelopment() : SyfosmreglerClient {
    override suspend fun sjekkRegler(receivedSykmelding: ReceivedSykmelding): ValidationResult {
        return ValidationResult(Status.OK, listOf(RuleInfo("regel", "", "", Status.OK)))
    }
}
