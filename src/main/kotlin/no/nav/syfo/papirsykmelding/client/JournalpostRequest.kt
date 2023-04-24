package no.nav.syfo.papirsykmelding.client

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JournalpostRequest(
    val avsenderMottaker: AvsenderMottaker? = null,
    val behandlingstema: String? = null,
    var bruker: Bruker,
    val dokumenter: List<Dokument>,
    val eksternReferanseId: String = UUID.randomUUID().toString(),
    val journalfoerendeEnhet: String? = null,
    val journalpostType: String = "INNGAAENDE",
    val kanal: String = "SKAN_IM",
    val tema: String = "SYM",
    val tittel: String = "Papirsykmelding fra Sorlandet sykehus",
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AvsenderMottaker(
    val id: String? = null,
    val idType: String? = null,
    val land: String? = null,
    val navn: String,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Bruker(
    val id: String,
    val idType: String = "FNR",
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Dokument(
    val brevkode: String = "NAV 08-07.04",
    val dokumentKategori: String = "IS",
    val dokumentvarianter: List<Dokumentvarianter>,
    val tittel: String = "Papirsykmelding",
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Dokumentvarianter(
    val filnavn: String,
    val filtype: String,
    val fysiskDokument: String,
    val variantformat: String,
)
