package no.nav.syfo.sykmelding

import java.time.LocalDate
import no.nav.helse.diagnosekoder.Diagnosekoder
import no.nav.syfo.model.SykmeldingPeriode
import no.nav.syfo.model.SykmeldingType
import no.nav.syfo.pdl.model.Navn
import no.nav.syfo.pdl.model.PdlPerson
import no.nav.syfo.sykmelding.model.AnnenFraverGrunn
import no.nav.syfo.sykmelding.model.Diagnose
import no.nav.syfo.sykmelding.model.SykmeldingRequest
import no.nav.syfo.sykmelding.model.UtdypendeOpplysninger
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.jupiter.api.Test

internal class SykmeldingXmlUtilKtTest {
    @Test
    internal fun `Helseopplysninger opprettes korrekt for minimal request`() {
        val sykmeldingRequest =
            SykmeldingRequest(
                fnr = "12345678910",
                fnrLege = "10987654321",
                herId = null,
                hprNummer = null,
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                annenFraverGrunn = null,
                perioder =
                    listOf(
                        SykmeldingPeriode(
                            fom = LocalDate.now().plusDays(1),
                            tom = LocalDate.now().plusWeeks(1),
                            type = SykmeldingType.HUNDREPROSENT,
                        ),
                    ),
                behandletDato = LocalDate.now(),
                kontaktDato = null,
                begrunnIkkeKontakt = null,
                vedlegg = false,
                virksomhetsykmelding = false,
                utdypendeOpplysninger = UtdypendeOpplysninger.INGEN,
                regelsettVersjon = "2",
                meldingTilArbeidsgiver = null,
                bidiagnoser = emptyList(),
                arbeidsgiverNavn = null,
                vedleggMedVirus = false,
                beskrivBistandNav = null,
                yrkesskade = false,
                hoveddiagnose = Diagnose(code = "A02", system = "ICPC2", text = "")
            )
        val sykmeldt = PdlPerson(Navn("Syk", null, "Sykestad"))
        val lege = PdlPerson(Navn("Doktor", null, "Dyregod"))

        val helseopplysninger = lagHelseopplysninger(sykmeldingRequest, sykmeldt, lege)

        helseopplysninger.pasient.fodselsnummer.id shouldBeEqualTo "12345678910"
        helseopplysninger.pasient.navn.fornavn shouldBeEqualTo "Syk"
        helseopplysninger.pasient.navn.etternavn shouldBeEqualTo "Sykestad"
        helseopplysninger.medisinskVurdering.hovedDiagnose.diagnosekode.v shouldBeEqualTo "A02"
        helseopplysninger.medisinskVurdering.hovedDiagnose.diagnosekode.s shouldBeEqualTo
            "2.16.578.1.12.4.1.1.7170"
        helseopplysninger.medisinskVurdering.hovedDiagnose.diagnosekode.dn shouldBeEqualTo
            "Frysninger"
        helseopplysninger.aktivitet.periode[0].periodeFOMDato shouldBeEqualTo
            LocalDate.now().plusDays(1)
        helseopplysninger.aktivitet.periode[0].periodeTOMDato shouldBeEqualTo
            LocalDate.now().plusWeeks(1)
        helseopplysninger.aktivitet.periode[0]
            .aktivitetIkkeMulig
            .medisinskeArsaker
            .beskriv shouldBeEqualTo "medisinske årsaker til sykefravær"
        helseopplysninger.kontaktMedPasient.behandletDato shouldBeEqualTo
            LocalDate.now().atStartOfDay()
        helseopplysninger.kontaktMedPasient.kontaktDato shouldBeEqualTo null
        helseopplysninger.behandler.id[0].id shouldBeEqualTo "10987654321"
        helseopplysninger.utdypendeOpplysninger shouldBeEqualTo null
    }

    @Test
    internal fun `Helseopplysninger opprettes korrekt for maksimal request`() {
        val sykmeldingRequest =
            SykmeldingRequest(
                fnr = "12345678910",
                fnrLege = "10987654321",
                herId = "herId",
                hprNummer = "hpr",
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                annenFraverGrunn = AnnenFraverGrunn.SMITTEFARE,
                perioder =
                    listOf(
                        SykmeldingPeriode(
                            fom = LocalDate.now().minusWeeks(2),
                            tom = LocalDate.now().minusDays(1),
                            type = SykmeldingType.HUNDREPROSENT,
                        ),
                        SykmeldingPeriode(
                            fom = LocalDate.now().plusDays(1),
                            tom = LocalDate.now().plusWeeks(1),
                            type = SykmeldingType.GRADERT_REISETILSKUDD,
                        ),
                    ),
                behandletDato = LocalDate.now(),
                kontaktDato = LocalDate.now().minusDays(2),
                begrunnIkkeKontakt = "Hadde ikke tid",
                vedlegg = false,
                virksomhetsykmelding = false,
                utdypendeOpplysninger = UtdypendeOpplysninger.UKE_39,
                regelsettVersjon = "2",
                meldingTilArbeidsgiver = "Melding til arbeidsgiver",
                bidiagnoser = null,
                arbeidsgiverNavn = null,
                vedleggMedVirus = false,
                beskrivBistandNav = null,
                yrkesskade = false,
                hoveddiagnose = Diagnose(code = "M674", system = "icd10", text = "")
            )
        val sykmeldt = PdlPerson(Navn("Syk", null, "Sykestad"))
        val lege = PdlPerson(Navn("Doktor", null, "Dyregod"))

        val helseopplysninger = lagHelseopplysninger(sykmeldingRequest, sykmeldt, lege)

        helseopplysninger.pasient.fodselsnummer.id shouldBeEqualTo "12345678910"
        helseopplysninger.pasient.navn.fornavn shouldBeEqualTo "Syk"
        helseopplysninger.pasient.navn.etternavn shouldBeEqualTo "Sykestad"
        helseopplysninger.pasient.navnFastlege shouldBeEqualTo "Victor Frankenstein"
        helseopplysninger.medisinskVurdering.hovedDiagnose.diagnosekode.dn shouldBeEqualTo
            "Ganglion"
        helseopplysninger.medisinskVurdering.annenFraversArsak.arsakskode[0].dn shouldBeEqualTo
            "Når vedkommende myndighet har nedlagt forbud mot at han eller hun arbeider på grunn av smittefare"
        helseopplysninger.medisinskVurdering.annenFraversArsak.arsakskode[0].v shouldBeEqualTo "6"
        helseopplysninger.aktivitet.periode[0].periodeFOMDato shouldBeEqualTo
            LocalDate.now().minusWeeks(2)
        helseopplysninger.aktivitet.periode[0].periodeTOMDato shouldBeEqualTo
            LocalDate.now().minusDays(1)
        helseopplysninger.aktivitet.periode[0].aktivitetIkkeMulig shouldNotBeEqualTo null
        helseopplysninger.aktivitet.periode[1].periodeFOMDato shouldBeEqualTo
            LocalDate.now().plusDays(1)
        helseopplysninger.aktivitet.periode[1].periodeTOMDato shouldBeEqualTo
            LocalDate.now().plusWeeks(1)
        helseopplysninger.aktivitet.periode[1].gradertSykmelding.sykmeldingsgrad shouldBeEqualTo 60
        helseopplysninger.aktivitet.periode[1].gradertSykmelding.isReisetilskudd shouldBeEqualTo
            true
        helseopplysninger.kontaktMedPasient.behandletDato shouldBeEqualTo
            LocalDate.now().atStartOfDay()
        helseopplysninger.kontaktMedPasient.kontaktDato shouldBeEqualTo LocalDate.now().minusDays(2)
        helseopplysninger.kontaktMedPasient.begrunnIkkeKontakt shouldBeEqualTo "Hadde ikke tid"
        helseopplysninger.behandler.id[0].id shouldBeEqualTo "10987654321"
        helseopplysninger.utdypendeOpplysninger.spmGruppe[0].spmSvar.size shouldBeEqualTo 3
        helseopplysninger.utdypendeOpplysninger.spmGruppe[0].spmGruppeId shouldBeEqualTo "6.4"
        helseopplysninger.meldingTilArbeidsgiver shouldBeEqualTo "Melding til arbeidsgiver"
        helseopplysninger.meldingTilNav?.isBistandNAVUmiddelbart shouldBeEqualTo null
        helseopplysninger.meldingTilNav?.beskrivBistandNAV shouldBeEqualTo null
        helseopplysninger.prognose.erIArbeid shouldNotBeEqualTo null
        helseopplysninger.tiltak shouldNotBeEqualTo null
        helseopplysninger.regelSettVersjon shouldBeEqualTo "2"
    }

    @Test
    internal fun `Helseopplysninger opprettes korrekt for maksimal request og regelsettversjon 3`() {
        val sykmeldingRequest =
            SykmeldingRequest(
                fnr = "12345678910",
                fnrLege = "10987654321",
                herId = "herId",
                hprNummer = "hpr",
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                annenFraverGrunn = AnnenFraverGrunn.SMITTEFARE,
                perioder =
                    listOf(
                        SykmeldingPeriode(
                            fom = LocalDate.now().minusWeeks(2),
                            tom = LocalDate.now().minusDays(1),
                            type = SykmeldingType.HUNDREPROSENT,
                        ),
                        SykmeldingPeriode(
                            fom = LocalDate.now().plusDays(1),
                            tom = LocalDate.now().plusWeeks(1),
                            type = SykmeldingType.GRADERT_REISETILSKUDD,
                        ),
                    ),
                behandletDato = LocalDate.now(),
                kontaktDato = LocalDate.now().minusDays(2),
                begrunnIkkeKontakt = "Hadde ikke tid",
                vedlegg = false,
                virksomhetsykmelding = false,
                beskrivBistandNav = null,
                utdypendeOpplysninger = UtdypendeOpplysninger.UKE_39,
                regelsettVersjon = "3",
                meldingTilArbeidsgiver = "Viktig melding til arbeidsgiver",
                bidiagnoser =
                    listOf(
                        Diagnose(
                            code = "M674",
                            system = "icd10",
                            text = "Ein viktig diagnose",
                        ),
                        Diagnose(
                            code = "Z09",
                            system = "icpc2",
                            text = "Politi",
                        ),
                    ),
                arbeidsgiverNavn = "NAV",
                vedleggMedVirus = false,
                yrkesskade = false,
                hoveddiagnose = Diagnose(code = "M674", system = "icd10", text = "")
            )
        val sykmeldt = PdlPerson(Navn("Syk", null, "Sykestad"))
        val lege = PdlPerson(Navn("Doktor", null, "Dyregod"))

        val helseopplysninger = lagHelseopplysninger(sykmeldingRequest, sykmeldt, lege)

        helseopplysninger.pasient.fodselsnummer.id shouldBeEqualTo "12345678910"
        helseopplysninger.pasient.navn.fornavn shouldBeEqualTo "Syk"
        helseopplysninger.pasient.navn.etternavn shouldBeEqualTo "Sykestad"
        helseopplysninger.pasient.navnFastlege shouldBeEqualTo null
        helseopplysninger.medisinskVurdering.hovedDiagnose.diagnosekode.dn shouldBeEqualTo
            "Ganglion"
        helseopplysninger.medisinskVurdering.annenFraversArsak.arsakskode[0].dn shouldBeEqualTo
            "Når vedkommende myndighet har nedlagt forbud mot at han eller hun arbeider på grunn av smittefare"
        helseopplysninger.medisinskVurdering.annenFraversArsak.arsakskode[0].v shouldBeEqualTo "6"
        helseopplysninger.aktivitet.periode[0].periodeFOMDato shouldBeEqualTo
            LocalDate.now().minusWeeks(2)
        helseopplysninger.aktivitet.periode[0].periodeTOMDato shouldBeEqualTo
            LocalDate.now().minusDays(1)
        helseopplysninger.aktivitet.periode[0].aktivitetIkkeMulig shouldNotBeEqualTo null
        helseopplysninger.aktivitet.periode[1].periodeFOMDato shouldBeEqualTo
            LocalDate.now().plusDays(1)
        helseopplysninger.aktivitet.periode[1].periodeTOMDato shouldBeEqualTo
            LocalDate.now().plusWeeks(1)
        helseopplysninger.aktivitet.periode[1].gradertSykmelding.sykmeldingsgrad shouldBeEqualTo 60
        helseopplysninger.aktivitet.periode[1].gradertSykmelding.isReisetilskudd shouldBeEqualTo
            true
        helseopplysninger.kontaktMedPasient.behandletDato shouldBeEqualTo
            LocalDate.now().atStartOfDay()
        helseopplysninger.kontaktMedPasient.kontaktDato shouldBeEqualTo LocalDate.now().minusDays(2)
        helseopplysninger.kontaktMedPasient.begrunnIkkeKontakt shouldBeEqualTo "Hadde ikke tid"
        helseopplysninger.behandler.id[0].id shouldBeEqualTo "10987654321"
        helseopplysninger.utdypendeOpplysninger.spmGruppe[0].spmSvar.size shouldBeEqualTo 3
        helseopplysninger.utdypendeOpplysninger.spmGruppe[0].spmGruppeId shouldBeEqualTo "6.5"
        helseopplysninger.meldingTilArbeidsgiver shouldBeEqualTo "Viktig melding til arbeidsgiver"
        helseopplysninger.meldingTilNav?.isBistandNAVUmiddelbart shouldBeEqualTo null
        helseopplysninger.meldingTilNav?.beskrivBistandNAV shouldBeEqualTo null
        helseopplysninger.prognose.erIArbeid shouldBeEqualTo null
        helseopplysninger.tiltak shouldBeEqualTo null
        helseopplysninger.regelSettVersjon shouldBeEqualTo "3"
        helseopplysninger.medisinskVurdering.biDiagnoser.diagnosekode[0].v shouldBeEqualTo "M674"
        helseopplysninger.medisinskVurdering.biDiagnoser.diagnosekode[1].v shouldBeEqualTo "Z09"
        helseopplysninger.arbeidsgiver.navnArbeidsgiver shouldBeEqualTo "NAV"
    }

    @Test
    internal fun `Helseopplysninger kan opprettes med tullekode som diagnosekode (teste avvist sykmelding)`() {
        val sykmeldingRequest =
            SykmeldingRequest(
                fnr = "12345678910",
                fnrLege = "10987654321",
                herId = null,
                hprNummer = null,
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                annenFraverGrunn = null,
                perioder =
                    listOf(
                        SykmeldingPeriode(
                            fom = LocalDate.now().plusDays(1),
                            tom = LocalDate.now().plusWeeks(1),
                            type = SykmeldingType.HUNDREPROSENT,
                        ),
                    ),
                behandletDato = LocalDate.now(),
                kontaktDato = null,
                begrunnIkkeKontakt = null,
                vedlegg = false,
                virksomhetsykmelding = false,
                beskrivBistandNav = null,
                utdypendeOpplysninger = UtdypendeOpplysninger.UKE_39,
                regelsettVersjon = "2",
                meldingTilArbeidsgiver = null,
                bidiagnoser = emptyList(),
                arbeidsgiverNavn = null,
                vedleggMedVirus = false,
                yrkesskade = false,
                hoveddiagnose = Diagnose(code = "TULLEKODE", system = "icd10", text = "")
            )
        val sykmeldt = PdlPerson(Navn("Syk", null, "Sykestad"))
        val lege = PdlPerson(Navn("Doktor", null, "Dyregod"))

        val helseopplysninger = lagHelseopplysninger(sykmeldingRequest, sykmeldt, lege)

        helseopplysninger.medisinskVurdering.hovedDiagnose.diagnosekode.v shouldBeEqualTo
            "TULLEKODE"
        helseopplysninger.medisinskVurdering.hovedDiagnose.diagnosekode.s shouldBeEqualTo
            Diagnosekoder.ICD10_CODE
        helseopplysninger.medisinskVurdering.hovedDiagnose.diagnosekode.dn shouldBeEqualTo ""
        helseopplysninger.medisinskVurdering.biDiagnoser shouldBeEqualTo null
    }

    @Test
    internal fun `Virksomhetsykmelding faar riktig behandler`() {
        val sykmeldingRequest =
            SykmeldingRequest(
                fnr = "12345678910",
                fnrLege = "10987654321",
                herId = null,
                hprNummer = "hpr",
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                annenFraverGrunn = null,
                perioder =
                    listOf(
                        SykmeldingPeriode(
                            fom = LocalDate.now().plusDays(1),
                            tom = LocalDate.now().plusWeeks(1),
                            type = SykmeldingType.HUNDREPROSENT,
                        ),
                    ),
                behandletDato = LocalDate.now(),
                kontaktDato = null,
                begrunnIkkeKontakt = null,
                vedlegg = false,
                virksomhetsykmelding = true,
                utdypendeOpplysninger = UtdypendeOpplysninger.UKE_39,
                regelsettVersjon = "2",
                meldingTilArbeidsgiver = null,
                bidiagnoser = null,
                arbeidsgiverNavn = null,
                vedleggMedVirus = false,
                beskrivBistandNav = null,
                yrkesskade = false,
                hoveddiagnose = Diagnose(code = "M674", system = "icd10", text = "")
            )
        val sykmeldt = PdlPerson(Navn("Syk", null, "Sykestad"))
        val lege = PdlPerson(Navn("Doktor", null, "Dyregod"))

        val helseopplysninger = lagHelseopplysninger(sykmeldingRequest, sykmeldt, lege)

        helseopplysninger.behandler.id[0].id shouldBeEqualTo "10987654321"
    }

    @Test
    internal fun `BEHANDLINGSDAG gir en behandlingsdag pr uke`() {
        val sykmeldingRequest =
            SykmeldingRequest(
                fnr = "12345678910",
                fnrLege = "10987654321",
                herId = null,
                hprNummer = null,
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                annenFraverGrunn = null,
                perioder =
                    listOf(
                        SykmeldingPeriode(
                            fom = LocalDate.now(),
                            tom = LocalDate.now().plusDays(10),
                            type = SykmeldingType.BEHANDLINGSDAG,
                        ),
                    ),
                behandletDato = LocalDate.now(),
                kontaktDato = null,
                begrunnIkkeKontakt = null,
                vedlegg = false,
                virksomhetsykmelding = false,
                beskrivBistandNav = null,
                utdypendeOpplysninger = UtdypendeOpplysninger.INGEN,
                regelsettVersjon = "2",
                meldingTilArbeidsgiver = null,
                bidiagnoser = null,
                arbeidsgiverNavn = null,
                vedleggMedVirus = false,
                yrkesskade = false,
                hoveddiagnose = Diagnose(code = "M674", system = "icd10", text = "")
            )
        val sykmeldt = PdlPerson(Navn("Syk", null, "Sykestad"))
        val lege = PdlPerson(Navn("Doktor", null, "Dyregod"))

        val helseopplysninger = lagHelseopplysninger(sykmeldingRequest, sykmeldt, lege)

        helseopplysninger.aktivitet.periode[0].periodeFOMDato shouldBeEqualTo LocalDate.now()
        helseopplysninger.aktivitet.periode[0].periodeTOMDato shouldBeEqualTo
            LocalDate.now().plusDays(10)
        helseopplysninger.aktivitet.periode[0]
            .behandlingsdager
            .antallBehandlingsdagerUke shouldBeEqualTo 2
    }

    @Test
    internal fun `BEHANDLINGSDAGER gir en behandlingsdag pr uke pluss en`() {
        val sykmeldingRequest =
            SykmeldingRequest(
                fnr = "12345678910",
                fnrLege = "10987654321",
                herId = null,
                hprNummer = null,
                syketilfelleStartdato = LocalDate.now().minusDays(1),
                annenFraverGrunn = null,
                perioder =
                    listOf(
                        SykmeldingPeriode(
                            fom = LocalDate.now(),
                            tom = LocalDate.now().plusDays(10),
                            type = SykmeldingType.BEHANDLINGSDAGER,
                        ),
                    ),
                behandletDato = LocalDate.now(),
                kontaktDato = null,
                begrunnIkkeKontakt = null,
                vedlegg = false,
                virksomhetsykmelding = false,
                beskrivBistandNav = null,
                utdypendeOpplysninger = UtdypendeOpplysninger.INGEN,
                regelsettVersjon = "2",
                meldingTilArbeidsgiver = null,
                bidiagnoser = null,
                arbeidsgiverNavn = null,
                vedleggMedVirus = false,
                yrkesskade = false,
                hoveddiagnose = Diagnose(code = "M674", system = "icd10", text = "")
            )
        val sykmeldt = PdlPerson(Navn("Syk", null, "Sykestad"))
        val lege = PdlPerson(Navn("Doktor", null, "Dyregod"))

        val helseopplysninger = lagHelseopplysninger(sykmeldingRequest, sykmeldt, lege)

        helseopplysninger.aktivitet.periode[0].periodeFOMDato shouldBeEqualTo LocalDate.now()
        helseopplysninger.aktivitet.periode[0].periodeTOMDato shouldBeEqualTo
            LocalDate.now().plusDays(10)
        helseopplysninger.aktivitet.periode[0]
            .behandlingsdager
            .antallBehandlingsdagerUke shouldBeEqualTo 3
    }
}
