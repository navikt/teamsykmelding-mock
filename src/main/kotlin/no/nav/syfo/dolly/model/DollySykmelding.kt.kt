package no.nav.syfo.dolly.model

import io.ktor.http.HttpStatusCode
import java.time.LocalDate

data class Aktivitet(
    val fom: LocalDate,
    val tom: LocalDate,
    val grad: Int? = null,
)

data class DollySykmelding(
    val ident: String,
    val aktivitet: List<Aktivitet>,
)

data class DollySykmeldingResponse(
    val sykmeldingId: String,
    val ident: String,
    val aktivitet: List<Aktivitet>,
)

data class ErrorMessage(val message: String)

data class DollyOpprettSykmeldingResponse(
    val status: HttpStatusCode,
    val message: String,
)
