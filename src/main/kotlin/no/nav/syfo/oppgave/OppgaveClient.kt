package no.nav.syfo.oppgave

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.time.LocalDate
import no.nav.syfo.azuread.AccessTokenClientV2
import no.nav.syfo.utils.logger

interface OppgaveClient {
    suspend fun opprettOppgave(opprettOppgave: OpprettOppgave): OpprettOppgaveResponse
}

class OppgaveClientProduction(
    private val url: String,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val scope: String,
    private val httpClient: HttpClient,
) : OppgaveClient {
    override suspend fun opprettOppgave(opprettOppgave: OpprettOppgave): OpprettOppgaveResponse {
        logger.info("oppretter oppgave for ${opprettOppgave.journalpostId}")
        val response =
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                val token = accessTokenClientV2.getAccessTokenV2(scope)
                logger.info("got token for opprett oppgave")
                header("Authorization", "Bearer $token")
                header("X-Correlation-ID", opprettOppgave.journalpostId)
                setBody(opprettOppgave)
            }
        if (response.status == HttpStatusCode.Created) {
            return response.body()
        } else {
            throw RuntimeException(
                "Noe gikk galt ved oppretting av oppgave for journalpostId ${opprettOppgave.journalpostId}: ${response.status}, ${response.bodyAsText()}"
            )
        }
    }
}

class DevelopmentOppgaveClient : OppgaveClient {
    override suspend fun opprettOppgave(opprettOppgave: OpprettOppgave): OpprettOppgaveResponse {
        logger.info("later som vi oppretter oppgave for ${opprettOppgave.journalpostId}")
        return OpprettOppgaveResponse(1, 1)
    }
}

data class OpprettOppgave(
    val tildeltEnhetsnr: String? = null,
    val opprettetAvEnhetsnr: String? = null,
    val aktoerId: String? = null,
    val journalpostId: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val saksreferanse: String? = null,
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val tema: String? = null,
    val oppgavetype: String,
    val behandlingstype: String? = null,
    val aktivDato: LocalDate,
    val fristFerdigstillelse: LocalDate? = null,
    val prioritet: String,
    val behandlingsTema: Nothing?,
    val metadata: Map<String, String?> = emptyMap(),
    val personident: String? = null,
)

data class OpprettOppgaveResponse(
    val id: Int,
    val versjon: Int,
    val status: String? = null,
    val tildeltEnhetsnr: String? = null,
    val mappeId: Int? = null,
)
