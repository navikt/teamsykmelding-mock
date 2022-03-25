package no.nav.syfo.papirsykmelding.client

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JournalpostResponse(
    val dokumenter: List<DokumentInfo>,
    val journalpostId: String,
    val journalpostferdigstilt: Boolean
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DokumentInfo(
    val brevkode: String? = null,
    val dokumentInfoId: String? = null,
    val tittel: String? = null
)
