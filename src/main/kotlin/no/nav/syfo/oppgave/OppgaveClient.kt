package no.nav.syfo.oppgave

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import no.nav.syfo.azuread.AccessTokenClientV2
import no.nav.syfo.utils.logger

interface OppgaveClient {
    suspend fun opprettOppgave(opprettOppgave: OpprettOppgave): OpprettOppgaveResponse

    suspend fun getOppgaveId(journalpostId: String): OppgaveResponse
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

    override suspend fun getOppgaveId(journalpostId: String): OppgaveResponse {
        val accessToken = accessTokenClientV2.getAccessTokenV2(scope)

        val response: HttpResponse =
            httpClient.get(url) {
                header("Authorization", "Bearer $accessToken")
                header("X-Correlation-ID", UUID.randomUUID().toString())
                parameter("tema", "SYM")
                parameter("oppgavetype", "JFR")
                parameter("journalpostId", journalpostId)
                parameter("statuskategori", "AAPEN")
                parameter("sorteringsrekkefolge", "ASC")
                parameter("sorteringsfelt", "FRIST")
                parameter("limit", "10")
            }

        return response.body<OppgaveResponse>()
    }
}

class DevelopmentOppgaveClient : OppgaveClient {
    var oppgaveId = 1

    val oppgaver = mutableMapOf<String, Deferred<OppgaveResponse>>()

    override suspend fun opprettOppgave(opprettOppgave: OpprettOppgave): OpprettOppgaveResponse {
        logger.info("later som vi oppretter oppgave for ${opprettOppgave.journalpostId}")
        return OpprettOppgaveResponse(oppgaveId++, 1)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun getOppgaveId(journalpostId: String): OppgaveResponse {
        if (oppgaver.contains(journalpostId)) {
            val oppgave = oppgaver[journalpostId]

            if (oppgave?.isCompleted == true) {
                val result = oppgave.await()
                print("result: $result")
                return result
            } else return OppgaveResponse(antallTreffTotalt = 0, listOf())
        } else {

            val job =
                GlobalScope.async(Dispatchers.IO) {
                    delay((0..15).random() * 1000L)
                    OppgaveResponse(antallTreffTotalt = 1, listOf(Oppgave(id = oppgaveId++)))
                }
            oppgaver[journalpostId] = job
            return OppgaveResponse(antallTreffTotalt = 0, listOf())
        }
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

data class OppgaveResponse(
    val antallTreffTotalt: Int,
    val oppgaver: List<Oppgave>,
)

data class Oppgave(
    val id: Int,
)
