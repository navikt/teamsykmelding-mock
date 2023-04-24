package no.nav.syfo.utenlandsk.model

data class UtenlandskSykmeldingRequest(
    val fnr: String,
    val antallPdfs: Int = 1,
)
