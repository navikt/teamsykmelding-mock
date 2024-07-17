package no.nav.syfo.pdl.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import no.nav.syfo.pdl.client.model.GetPersonRequest
import no.nav.syfo.pdl.client.model.GetPersonResponse
import no.nav.syfo.pdl.client.model.GetPersonVariables

interface PdlClient {
    suspend fun getPersoner(fnrs: List<String>, token: String): GetPersonResponse
}

class ProductionPdlClient(
    private val httpClient: HttpClient,
    private val basePath: String,
    private val graphQlQuery: String,
) : PdlClient {
    private val temaHeader = "TEMA"
    private val tema = "SYM"

    override suspend fun getPersoner(fnrs: List<String>, token: String): GetPersonResponse {
        val getPersonRequest =
            GetPersonRequest(query = graphQlQuery, variables = GetPersonVariables(identer = fnrs))
        return httpClient
            .post(basePath) {
                setBody(getPersonRequest)
                header(HttpHeaders.Authorization, "Bearer $token")
                header("Behandlingsnummer", "B229")
                header(temaHeader, tema)
                header(HttpHeaders.ContentType, "application/json")
            }
            .body()
    }
}

class DevelopmentPdlClient : PdlClient {
    override suspend fun getPersoner(fnrs: List<String>, token: String): GetPersonResponse {
        TODO("Not yet implemented")
    }
}
