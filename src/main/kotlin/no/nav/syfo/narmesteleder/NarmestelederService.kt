package no.nav.syfo.narmesteleder

import no.nav.syfo.narmesteleder.api.OpprettNarmestelederRequest
import no.nav.syfo.narmesteleder.kafka.NlResponseProducer
import no.nav.syfo.narmesteleder.kafka.model.Leder
import no.nav.syfo.narmesteleder.kafka.model.NlResponse
import no.nav.syfo.narmesteleder.kafka.model.Sykmeldt
import no.nav.syfo.pdl.model.Navn
import no.nav.syfo.pdl.service.PdlPersonService

class NarmestelederService(
    private val nlResponseProducer: NlResponseProducer,
    private val pdlPersonService: PdlPersonService
) {
    suspend fun registrerNarmesteleder(opprettNarmestelederRequest: OpprettNarmestelederRequest) {
        val personer = pdlPersonService.getPersoner(listOf(opprettNarmestelederRequest.ansattFnr, opprettNarmestelederRequest.lederFnr))
        val sykmeldt = personer[opprettNarmestelederRequest.ansattFnr]
        val leder = personer[opprettNarmestelederRequest.lederFnr]

        nlResponseProducer.sendNlResponse(
            NlResponse(
                orgnummer = opprettNarmestelederRequest.orgnummer,
                utbetalesLonn = opprettNarmestelederRequest.forskutterer,
                leder = Leder(
                    fnr = opprettNarmestelederRequest.lederFnr,
                    mobil = opprettNarmestelederRequest.mobil,
                    epost = opprettNarmestelederRequest.epost,
                    fornavn = leder?.navn?.fornavn + if (leder?.navn?.mellomnavn != null) leder.navn.mellomnavn else "",
                    etternavn = leder?.navn?.etternavn ?: "Etternavn"
                ),
                sykmeldt = Sykmeldt(
                    fnr = opprettNarmestelederRequest.ansattFnr,
                    navn = getName(sykmeldt?.navn)
                )
            )
        )
    }

    fun getName(person: Navn?): String {
        return if (person?.mellomnavn == null) {
            "${person?.fornavn} ${person?.etternavn}"
        } else {
            "${person.fornavn} ${person.mellomnavn} ${person.etternavn}"
        }
    }
}
