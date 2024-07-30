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
import java.io.IOException
import no.nav.syfo.azuread.AccessTokenClientV2
import no.nav.syfo.logger
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult

interface SyfosmpapirreglerClient {
    suspend fun sjekkRegler(receivedSykmelding: ReceivedSykmelding): ValidationResult
}

class SyfosmpapirreglerClientProduction(
    private val syfosmpapirreglerUrl: String,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val syfosmpapirreglerScope: String,
    private val httpClient: HttpClient,
) : SyfosmpapirreglerClient {
    override suspend fun sjekkRegler(receivedSykmelding: ReceivedSykmelding): ValidationResult {
        val accessToken = accessTokenClientV2.getAccessTokenV2(syfosmpapirreglerScope)
        val httpResponse =
            httpClient.post("$syfosmpapirreglerUrl/api/v2/rules/validate") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                setBody(receivedSykmelding)
            }
        if (httpResponse.status == HttpStatusCode.OK) {
            return httpResponse.body<ValidationResult>()
        } else {
            logger.error(
                "Syfosmpapirregler svarte med feilkode ${httpResponse.status} for ${receivedSykmelding.sykmelding.msgId}"
            )
            throw IOException("Syfosmpapirregler svarte med feilkode ${httpResponse.status}")
        }
    }
}

class SyfosmpapirreglerClientDevelopment() : SyfosmpapirreglerClient {
    override suspend fun sjekkRegler(receivedSykmelding: ReceivedSykmelding): ValidationResult {
        logger.info("later som vi sjekker regler for ${receivedSykmelding.sykmelding.id}")
        return ValidationResult(
            status = Status.OK,
            ruleHits = listOf(RuleInfo("testregel", "", "", Status.OK))
        )
    }
}
