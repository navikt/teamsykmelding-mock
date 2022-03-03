package no.nav.syfo.sykmelding

import io.kotest.core.spec.style.FunSpec
import no.nav.syfo.pdl.model.Navn
import no.nav.syfo.pdl.model.PdlPerson
import no.nav.syfo.sykmelding.model.SykmeldingPeriode
import no.nav.syfo.sykmelding.model.SykmeldingRequest
import no.nav.syfo.sykmelding.model.SykmeldingType
import org.amshove.kluent.shouldBeEqualTo
import java.time.LocalDate

class SykmeldingXmlUtilKtTest : FunSpec({

    context("LagHelseopplysninger") {
        test("Helseopplysninger opprettes korrekt for minimal request") {
            val sykmeldingRequest = SykmeldingRequest(
                fnr = "12345678910",
                mottakId = "mottakId",
                fnrLege = "10987654321",
                msgId = "msgId",
                herId = null,
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                diagnosekode = "M674",
                annenFraverGrunn = null,
                perioder = listOf(
                    SykmeldingPeriode(
                        fom = LocalDate.now().plusDays(1),
                        tom = LocalDate.now().plusWeeks(1),
                        type = SykmeldingType.HUNDREPROSENT
                    )
                ),
                behandletDato = LocalDate.now(),
                kontaktDato = null,
                begrunnIkkeKontakt = null,
                vedlegg = false
            )
            val sykmeldt = PdlPerson(Navn("Syk", null, "Sykestad"))
            val lege = PdlPerson(Navn("Doktor", null, "Dyregod"))

            val helseopplysninger = lagHelseopplysninger(sykmeldingRequest, sykmeldt, lege)

            helseopplysninger.pasient.fodselsnummer.id shouldBeEqualTo "12345678910"
            helseopplysninger.pasient.navn.fornavn shouldBeEqualTo "Syk"
            helseopplysninger.pasient.navn.etternavn shouldBeEqualTo "Sykestad"
            helseopplysninger.medisinskVurdering.hovedDiagnose.diagnosekode.dn shouldBeEqualTo "Ganglion"
            helseopplysninger.kontaktMedPasient.behandletDato shouldBeEqualTo LocalDate.now().atStartOfDay()
            helseopplysninger.kontaktMedPasient.kontaktDato shouldBeEqualTo null
            helseopplysninger.behandler.id[0].id shouldBeEqualTo "10987654321"
        }
    }
})
