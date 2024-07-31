package no.nav.syfo.papirsykmelding.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.syfo.azuread.AccessTokenClientV2
import no.nav.syfo.utils.logger

interface DokarkivClient {
    suspend fun opprettJournalpost(
        journalpostRequest: JournalpostRequest,
    ): String
}

class DokarkivClientProduction(
    private val url: String,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val scope: String,
    private val httpClient: HttpClient,
) : DokarkivClient {
    override suspend fun opprettJournalpost(
        journalpostRequest: JournalpostRequest,
    ): String =
        try {
            logger.info("Oppretter papirsykmelding i dokarkiv")
            logger.info(
                "journalpostRequest info {}",
                kv("fnr", journalpostRequest.bruker?.id),
            )
            val token = accessTokenClientV2.getAccessTokenV2(scope)
            logger.info("Got access_token for dokarkiv")
            val httpResponse =
                httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $token")
                    header("Nav-Callid", journalpostRequest.eksternReferanseId)
                    setBody(journalpostRequest)
                    parameter("forsoekFerdigstill", false)
                }
            if (
                httpResponse.status == HttpStatusCode.Created ||
                    httpResponse.status == HttpStatusCode.Conflict
            ) {
                httpResponse.body<JournalpostResponse>().journalpostId
            } else {
                logger.error("Mottok uventet statuskode fra dokarkiv: {}", httpResponse.status)
                throw RuntimeException(
                    "Mottok uventet statuskode fra dokarkiv: ${httpResponse.status}"
                )
            }
        } catch (e: Exception) {
            logger.warn("Oppretting av journalpost feilet: ${e.message}")
            throw e
        }
}

fun opprettUtenlandskJournalpost(
    fnr: String?,
    pdf: String,
    antallPdfs: Int,
): JournalpostRequest {
    val dokumenter =
        (0 until antallPdfs).map {
            Dokument(
                dokumentvarianter =
                    mutableListOf(
                        Dokumentvarianter(
                            filnavn = "pdf-sykmelding-$it",
                            filtype = "PDFA",
                            variantformat = "ARKIV",
                            fysiskDokument = pdf,
                        ),
                    ),
                tittel = "Sykmelding-doc-$it",
            )
        }
    if (fnr.isNullOrEmpty()) {
        return JournalpostRequest(
            dokumenter = dokumenter,
        )
    }

    return JournalpostRequest(
        bruker = Bruker(id = fnr),
        dokumenter = dokumenter,
    )
}

fun opprettUtenlandskNavNoJournalpost(
    fnr: String?,
    pdf: String,
): JournalpostRequest {
    val dokumenter =
        (0 until 1).map {
            Dokument(
                dokumentvarianter =
                    mutableListOf(
                        Dokumentvarianter(
                            filnavn = "pdf-sykmelding-$it",
                            filtype = "PDFA",
                            variantformat = "ARKIV",
                            fysiskDokument = pdf,
                        )
                    ),
                tittel = "Egenerklæring for utenlandske sykemeldinger-$it",
            )
        }
    if (fnr.isNullOrEmpty()) {
        return JournalpostRequest(
            dokumenter = dokumenter,
            kanal = "NAV_NO",
            tema = "SYK",
            tittel = "Egenerklæring for utenlandske sykemeldinger",
        )
    }
    return JournalpostRequest(
        bruker = Bruker(id = fnr),
        dokumenter = dokumenter,
        kanal = "NAV_NO",
        tema = "SYK",
        tittel = "Egenerklæring for utenlandske sykemeldinger",
    )
}

fun opprettJournalpostPayload(
    fnr: String?,
    ocr: String?,
    pdf: String,
    metadata: String,
): JournalpostRequest {
    val dokumentvarianter =
        mutableListOf(
            Dokumentvarianter(
                filnavn = "pdf-sykmelding",
                filtype = "PDFA",
                variantformat = "ARKIV",
                fysiskDokument = pdf,
            ),
            Dokumentvarianter(
                filnavn = "xml-sykmeldingmetadata",
                filtype = "XML",
                variantformat = "FULLVERSJON",
                fysiskDokument = metadata,
            ),
        )
    if (ocr != null) {
        dokumentvarianter.add(
            Dokumentvarianter(
                filnavn = "ocr-sykmelding",
                filtype = "XML",
                variantformat = "ORIGINAL",
                fysiskDokument = ocr,
            ),
        )
    }
    if (fnr.isNullOrEmpty()) {
        return JournalpostRequest(
            dokumenter =
                listOf(
                    Dokument(dokumentvarianter = dokumentvarianter),
                ),
        )
    }
    return JournalpostRequest(
        bruker = Bruker(id = fnr),
        dokumenter =
            listOf(
                Dokument(dokumentvarianter = dokumentvarianter),
            ),
    )
}

fun opprettUtenlandskJournalpostPayload(
    fnr: String?,
    pdf: String,
    metadata: String,
): JournalpostRequest {
    val dokumentvarianter =
        mutableListOf(
            Dokumentvarianter(
                filnavn = "pdf-sykmelding",
                filtype = "PDFA",
                variantformat = "ARKIV",
                fysiskDokument = pdf,
            ),
            Dokumentvarianter(
                filnavn = "xml-sykmeldingmetadata",
                filtype = "XML",
                variantformat = "FULLVERSJON",
                fysiskDokument = metadata,
            ),
        )
    if (fnr.isNullOrEmpty()) {
        return JournalpostRequest(
            dokumenter =
                listOf(
                    Dokument(
                        brevkode = "NAV 08-07.04 U",
                        dokumentvarianter = dokumentvarianter,
                    ),
                ),
            tittel = "Utenlandsk papirsykmelding",
        )
    }
    return JournalpostRequest(
        bruker = Bruker(id = fnr),
        dokumenter =
            listOf(
                Dokument(
                    brevkode = "NAV 08-07.04 U",
                    dokumentvarianter = dokumentvarianter,
                ),
            ),
        tittel = "Utenlandsk papirsykmelding",
    )
}

class DokarkivClientDevelopment() : DokarkivClient {
    override suspend fun opprettJournalpost(journalpostRequest: JournalpostRequest): String {
        logger.info(
            "later som vi oppretter journalpost i dokarkiv: ${journalpostRequest.eksternReferanseId}"
        )
        return journalpostRequest.eksternReferanseId
    }
}
