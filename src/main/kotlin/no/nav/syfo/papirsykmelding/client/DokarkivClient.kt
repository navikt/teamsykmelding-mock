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
import no.nav.syfo.azuread.AccessTokenClient
import no.nav.syfo.log

class DokarkivClient(
    private val url: String,
    private val accessTokenClient: AccessTokenClient,
    private val scope: String,
    private val httpClient: HttpClient,
) {
    suspend fun opprettJournalpost(
        journalpostRequest: JournalpostRequest,
    ): String =
        try {
            log.info("Oppretter papirsykmelding i dokarkiv")
            val token = accessTokenClient.getAccessToken(scope)
            log.info("Got access_token for dokarkiv")
            val httpResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                header("Nav-Callid", journalpostRequest.eksternReferanseId)
                setBody(journalpostRequest)
                parameter("forsoekFerdigstill", false)
            }
            if (httpResponse.status == HttpStatusCode.Created || httpResponse.status == HttpStatusCode.Conflict) {
                httpResponse.body<JournalpostResponse>().journalpostId
            } else {
                log.error("Mottok uventet statuskode fra dokarkiv: {}, {}", httpResponse.status)
                throw RuntimeException("Mottok uventet statuskode fra dokarkiv: ${httpResponse.status}")
            }
        } catch (e: Exception) {
            log.warn("Oppretting av journalpost feilet: ${e.message}, {}")
            throw e
        }
}

fun opprettUtenlandskJournalpost(
    fnr: String,
    pdf: String,
    antallPdfs: Int,
): JournalpostRequest {
    return JournalpostRequest(
        bruker = Bruker(id = fnr),
        dokumenter = (0 until antallPdfs).map {
            Dokument(
                dokumentvarianter = mutableListOf(
                    Dokumentvarianter(
                        filnavn = "pdf-sykmelding-$it",
                        filtype = "PDFA",
                        variantformat = "ARKIV",
                        fysiskDokument = pdf,
                    ),
                ),
                tittel = "Sykmelding-doc-$it",
            )
        },
    )
}

fun opprettUtenlandskNavNoJournalpost(
    fnr: String,
    pdf: String,
): JournalpostRequest {
    return JournalpostRequest(
        bruker = Bruker(id = fnr),
        dokumenter = (0 until 1).map {
            Dokument(
                dokumentvarianter = mutableListOf(
                    Dokumentvarianter(
                        filnavn = "pdf-sykmelding-$it",
                        filtype = "PDFA",
                        variantformat = "ARKIV",
                        fysiskDokument = pdf,
                    ),
                ),
                tittel = "Egenerklæring for utenlandske sykemeldinger-$it",
            )
        },
        kanal = "NAV_NO",
        tema = "SYK",
        tittel = "Egenerklæring for utenlandske sykemeldinger"
    )
}

fun opprettJournalpostPayload(
    fnr: String,
    ocr: String?,
    pdf: String,
    metadata: String,
): JournalpostRequest {
    val dokumentvarianter = mutableListOf(
        Dokumentvarianter(
            filnavn = "pdf-sykmelding",
            filtype = "PDFA",
            variantformat = "ARKIV",
            fysiskDokument = pdf,
        ),
        Dokumentvarianter(
            filnavn = "xml-sykmeldingmetadata",
            filtype = "XML",
            variantformat = "SKANNING_META",
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
    return JournalpostRequest(
        bruker = Bruker(id = fnr),
        dokumenter = listOf(
            Dokument(dokumentvarianter = dokumentvarianter),
        ),
    )
}

fun opprettUtenlandskJournalpostPayload(
    fnr: String,
    pdf: String,
    metadata: String,
): JournalpostRequest {
    val dokumentvarianter = mutableListOf(
        Dokumentvarianter(
            filnavn = "pdf-sykmelding",
            filtype = "PDFA",
            variantformat = "ARKIV",
            fysiskDokument = pdf,
        ),
        Dokumentvarianter(
            filnavn = "xml-sykmeldingmetadata",
            filtype = "XML",
            variantformat = "SKANNING_META",
            fysiskDokument = metadata,
        ),
    )
    return JournalpostRequest(
        bruker = Bruker(id = fnr),
        dokumenter = listOf(
            Dokument(
                brevkode = "NAV 08-07.04 U",
                dokumentvarianter = dokumentvarianter,
            ),
        ),
        tittel = "Utenlandsk papirsykmelding",
    )
}
