package no.nav.syfo.pdl.model

data class PdlPerson(
    val navn: Navn
)

data class Navn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String
)
