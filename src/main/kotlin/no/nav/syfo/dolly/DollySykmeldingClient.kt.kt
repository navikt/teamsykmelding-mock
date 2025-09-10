package no.nav.syfo.dolly

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.syfo.dolly.model.DollySykmelding
import no.nav.syfo.dolly.model.DollySykmeldingResponse
import org.slf4j.LoggerFactory

interface DollyClient {
    suspend fun opprettSykmelding(sykmelding: DollySykmelding): DollySykmeldingResponse
}

class DollyClientProduction(
    private val url: String,
    private val httpClient: HttpClient,
) : DollyClient {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun opprettSykmelding(sykmelding: DollySykmelding): DollySykmeldingResponse {
        log.info("Oppretter sykmelding for ${sykmelding}")
        val response =
            httpClient.post("$url/api/sykmelding") {
                contentType(ContentType.Application.Json)
                setBody(sykmelding)
            }
        if (response.status == HttpStatusCode.OK) {
            val response: DollySykmeldingResponse = response.body()
            log.info("Opprettet sykmelding med input-dolly med id ${response.sykmeldingId}")
            return response
        } else {
            throw RuntimeException(
                "Noe gikk galt ved oppretting av sykmelding: ${response.status}, ${response.bodyAsText()}"
            )
        }
    }
}
