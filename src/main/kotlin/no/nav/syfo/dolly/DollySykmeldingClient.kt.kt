package no.nav.syfo.dolly

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.syfo.dolly.model.DollyHentResponse
import no.nav.syfo.dolly.model.DollyHentSykmeldingResponse
import no.nav.syfo.dolly.model.DollyResponse
import no.nav.syfo.dolly.model.DollySykmelding
import no.nav.syfo.dolly.model.DollySykmeldingResponse
import no.nav.syfo.dolly.model.ErrorMessage
import no.nav.syfo.utils.logger

interface DollyClient {
    suspend fun opprettSykmelding(sykmelding: DollySykmelding): DollyResponse

    suspend fun hentSykmelding(sykmeldingId: String): DollyHentResponse

    suspend fun slettSykmeldinger(ident: String): DollyResponse
}

class DollyClientProduction(
    private val url: String,
    private val httpClient: HttpClient,
) : DollyClient {
    override suspend fun opprettSykmelding(sykmelding: DollySykmelding): DollyResponse {
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
                return DollyResponse(
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
                return DollyResponse(
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

    override suspend fun hentSykmelding(sykmeldingId: String): DollyHentResponse {
        val response = httpClient.get("$url/api/sykmelding/$sykmeldingId")

        when (response.status) {
            HttpStatusCode.OK -> {
                val sykmeldingResponse = response.body<DollySykmeldingResponse>()
                logger.info(
                    "Hentet sykmelding med input-dolly med id ${sykmeldingResponse.sykmeldingId}"
                )
                return DollyHentResponse(
                    status = response.status,
                    message =
                        DollyHentSykmeldingResponse(
                            message =
                                "Hentet sykmelding med sykmeldingId ${sykmeldingResponse.sykmeldingId}",
                            sykmelding =
                                DollySykmeldingResponse(
                                    sykmeldingId = sykmeldingResponse.sykmeldingId,
                                    ident = sykmeldingResponse.ident,
                                    aktivitet = sykmeldingResponse.aktivitet,
                                )
                        )
                )
            }
            HttpStatusCode.NotFound,
            HttpStatusCode.InternalServerError -> {
                val errorResponse = response.body<ErrorMessage>()
                logger.error(
                    "Feil ved henting av sykmelding med input-dolly: ${errorResponse.message}"
                )
                return DollyHentResponse(
                    status = response.status,
                    message =
                        DollyHentSykmeldingResponse(
                            message = errorResponse.message,
                            sykmelding = null
                        )
                )
            }
            else -> {
                throw RuntimeException(
                    "Noe gikk galt ved henting av sykmelding: ${response.status}, ${response.bodyAsText()}"
                )
            }
        }
    }

    override suspend fun slettSykmeldinger(ident: String): DollyResponse {
        val response = httpClient.delete("$url/api/sykmelding/ident") { header("X-ident", ident) }

        when (response.status) {
            HttpStatusCode.OK -> {
                logger.info("Slettet sykmeldinger for ident")
                return DollyResponse(
                    status = response.status,
                    message = "Slettet sykmeldinger for ident",
                )
            }
            HttpStatusCode.InternalServerError -> {
                val errorResponse = response.body<ErrorMessage>()
                logger.error(
                    "Feil ved sletting av sykmeldinger med input-dolly: ${errorResponse.message}"
                )
                return DollyResponse(
                    status = response.status,
                    message = errorResponse.message,
                )
            }
            else -> {
                throw RuntimeException(
                    "Noe gikk galt ved sletting av sykmeldinger: ${response.status}, ${response.bodyAsText()}"
                )
            }
        }
    }
}
