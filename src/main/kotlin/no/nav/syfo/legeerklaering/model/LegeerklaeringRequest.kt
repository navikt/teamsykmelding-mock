package no.nav.syfo.legeerklaering.model

data class LegeerklaeringRequest(
    val fnr: String,
    val mottakId: String,
    val msgId: String,
    val fnrLege: String,
    val diagnosekode: String,
    val statusPresens: String?,
    val vedlegg: Boolean
)
