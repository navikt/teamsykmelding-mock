package no.nav.syfo.dolly

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.syfo.dolly.model.DollyOpprettSykmeldingResponse
import no.nav.syfo.dolly.model.DollySykmelding
import no.nav.syfo.dolly.model.DollySykmeldingResponse
import no.nav.syfo.dolly.model.ErrorMessage
import no.nav.syfo.utils.logger

interface DollyClient {
    suspend fun opprettSykmelding(sykmelding: DollySykmelding): DollyOpprettSykmeldingResponse
}

class DollyClientProduction(
    private val url: String,
    private val httpClient: HttpClient,
) : DollyClient {
    override suspend fun opprettSykmelding(
        sykmelding: DollySykmelding
    ): DollyOpprettSykmeldingResponse {
        logger.info("Oppretter sykmelding for ${sykmelding}")
        val response =
            httpClient.post("$url/api/sykmelding") {
                contentType(ContentType.Application.Json)
                setBody(sykmelding)
            }

        when (response.status) {
            HttpStatusCode.OK -> {
                val sykmeldingResponse = response.body<DollySykmeldingResponse>()
                logger.info(
                    "Opprettet sykmelding med input-dolly med id ${sykmeldingResponse.sykmeldingId}"
                )
                return DollyOpprettSykmeldingResponse(
                    status = response.status,
                    message =
                        "Opprettet sykmelding med sykmeldingId ${sykmeldingResponse.sykmeldingId}",
                )
            }
            HttpStatusCode.BadRequest,
            HttpStatusCode.InternalServerError -> {
                val errorResponse = response.body<ErrorMessage>()
                logger.error(
                    "Feil ved oppretting av sykmelding med input-dolly: ${errorResponse.message}"
                )
                return DollyOpprettSykmeldingResponse(
                    status = response.status,
                    message = errorResponse.message,
                )
            }
            else -> {
                throw RuntimeException(
                    "Noe gikk galt ved oppretting av sykmelding: ${response.status}, ${response.bodyAsText()}"
                )
            }
        }
    }
}
