package no.nav.syfo.pdl.service

import no.nav.syfo.azuread.AccessTokenClientV2
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.model.ResponseData
import no.nav.syfo.pdl.model.Navn
import no.nav.syfo.pdl.model.PdlPerson
import no.nav.syfo.utils.logger

interface PdlPersonService {
    suspend fun getPersoner(fnrs: List<String>): Map<String, PdlPerson>
}

class PdlPersonServiceProduction(
    private val pdlClient: PdlClient,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val pdlScope: String,
) : PdlPersonService {
    override suspend fun getPersoner(fnrs: List<String>): Map<String, PdlPerson> {
        val accessToken = accessTokenClientV2.getAccessTokenV2(pdlScope)
        val pdlResponse = pdlClient.getPersoner(fnrs = fnrs, token = accessToken)
        if (pdlResponse.errors != null) {
            pdlResponse.errors.forEach {
                logger.error("PDL returnerte feilmelding: ${it.message}, ${it.extensions?.code}")
                it.extensions?.details?.let { details ->
                    logger.error(
                        "Type: ${details.type}, cause: ${details.cause}, policy: ${details.policy}"
                    )
                }
            }
        }
        if (
            pdlResponse.data.hentPersonBolk == null ||
                pdlResponse.data.hentPersonBolk.isNullOrEmpty()
        ) {
            logger.error("Fant ikke identer i PDL")
            throw IllegalStateException("Fant ingen identer i PDL!")
        }
        pdlResponse.data.hentPersonBolk.forEach {
            if (it.code != "ok") {
                logger.warn("Mottok feilkode ${it.code} fra PDL for en eller flere personer")
            }
        }
        logger.info("Hentet personer fra PDL $pdlResponse")
        return pdlResponse.data.toPdlPersonMap()
    }

    private fun ResponseData.toPdlPersonMap(): Map<String, PdlPerson> {
        return hentPersonBolk!!
            .filter { it.person != null }
            .associate {
                it.ident to
                    PdlPerson(
                        navn = getNavn(it.person?.navn?.first()),
                    )
            }
    }

    private fun getNavn(navn: no.nav.syfo.pdl.client.model.Navn?): Navn =
        Navn(
            fornavn = navn?.fornavn ?: "Fornavn",
            mellomnavn = navn?.mellomnavn,
            etternavn = navn?.etternavn ?: "Etternavn"
        )
}

class PdlPersonServiceDevelopment() : PdlPersonService {
    override suspend fun getPersoner(fnrs: List<String>): Map<String, PdlPerson> {
        return mapOf(
            "person" to PdlPerson(Navn("Fornavn", null, "Etternavn")),
        )
    }
}
