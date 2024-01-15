package no.nav.syfo.utenlandsk.model

data class UtenlandskSykmeldingPdfRequest(
    val fnr: String?,
    val antallPdfs: Int = 1,
)

data class UtenlandskSykmeldingNavNoRequest(
    val fnr: String?,
)
