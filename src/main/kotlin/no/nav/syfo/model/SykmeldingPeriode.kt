package no.nav.syfo.model

import java.time.LocalDate

data class SykmeldingPeriode(
    val fom: LocalDate,
    val tom: LocalDate,
    val type: SykmeldingType,
)

enum class SykmeldingType {
    AVVENTENDE,
    GRADERT_UNDER_20,
    GRADERT_20,
    GRADERT_40,
    GRADERT_50,
    GRADERT_60,
    GRADERT_80,
    GRADERT_95,
    GRADERT_100,
    GRADERT_REISETILSKUDD,
    HUNDREPROSENT,
    BEHANDLINGSDAGER,
    BEHANDLINGSDAG,
    REISETILSKUDD,
}
