package no.nav.syfo.sykmelding

import no.nav.helse.msgHead.XMLCV
import no.nav.helse.msgHead.XMLIdent
import no.nav.helse.sm2013.Address
import no.nav.helse.sm2013.ArsakType
import no.nav.helse.sm2013.CS
import no.nav.helse.sm2013.CV
import no.nav.helse.sm2013.DynaSvarType
import no.nav.helse.sm2013.HelseOpplysningerArbeidsuforhet
import no.nav.helse.sm2013.Ident
import no.nav.helse.sm2013.NavnType
import no.nav.helse.sm2013.TeleCom
import no.nav.helse.sm2013.URL
import no.nav.syfo.model.SykmeldingPeriode
import no.nav.syfo.model.SykmeldingType
import no.nav.syfo.pdl.model.PdlPerson
import no.nav.syfo.sm.Diagnosekoder
import no.nav.syfo.sykmelding.model.AnnenFraverGrunn
import no.nav.syfo.sykmelding.model.SykmeldingRequest
import java.time.LocalDate
import java.time.ZoneId
import java.util.GregorianCalendar
import java.util.UUID
import javax.xml.datatype.DatatypeConfigurationException
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar

fun lagHelseopplysninger(
    sykmeldingRequest: SykmeldingRequest,
    sykmeldt: PdlPerson,
    lege: PdlPerson,
): HelseOpplysningerArbeidsuforhet {
    return HelseOpplysningerArbeidsuforhet().apply {
        syketilfelleStartDato = sykmeldingRequest.syketilfelleStartdato
        pasient = HelseOpplysningerArbeidsuforhet.Pasient().apply {
            navn = NavnType().apply {
                fornavn = sykmeldt.navn.fornavn
                mellomnavn = sykmeldt.navn.mellomnavn
                etternavn = sykmeldt.navn.etternavn
            }
            fodselsnummer = Ident().apply {
                id = sykmeldingRequest.fnr
                typeId = CV().apply {
                    dn = "Fødselsnummer"
                    s = "2.16.578.1.12.4.1.1.8116"
                    v = "FNR"
                }
            }
            navnFastlege = if (sykmeldingRequest.regelsettVersjon == "3") {
                null
            } else {
                "Victor Frankenstein"
            }
        }
        arbeidsgiver = HelseOpplysningerArbeidsuforhet.Arbeidsgiver().apply {
            harArbeidsgiver = CS().apply {
                dn = "Én arbeidsgiver"
                v = "1"
            }
            navnArbeidsgiver = "LOMMEN BARNEHAVE"
            yrkesbetegnelse = "Pedagogisk leder"
            stillingsprosent = 100
        }
        medisinskVurdering = medisinskVurdering(sykmeldingRequest.diagnosekode, sykmeldingRequest.diagnosekodesystem, sykmeldingRequest.annenFraverGrunn)
        aktivitet = HelseOpplysningerArbeidsuforhet.Aktivitet().apply {
            periode.addAll(sykmeldingRequest.perioder.map { tilPeriode(it) })
        }
        prognose = HelseOpplysningerArbeidsuforhet.Prognose().apply {
            isArbeidsforEtterEndtPeriode = true
            beskrivHensynArbeidsplassen = "Må ta det pent"
            erIArbeid = if (sykmeldingRequest.regelsettVersjon == "3") {
                null
            } else {
                HelseOpplysningerArbeidsuforhet.Prognose.ErIArbeid().apply {
                    isEgetArbeidPaSikt = true
                    isAnnetArbeidPaSikt = false
                    arbeidFraDato = sykmeldingRequest.perioder.maxOf { it.tom }
                    vurderingDato = sykmeldingRequest.perioder.minOf { it.fom }
                }
            }
        }
        tiltak = if (sykmeldingRequest.regelsettVersjon == "3") {
            null
        } else {
            HelseOpplysningerArbeidsuforhet.Tiltak().apply {
                tiltakArbeidsplassen = "Fortsett som sist."
                tiltakNAV = "Pasienten har plager som er kommet tilbake etter operasjon. Det er nylig tatt MR bildet som " +
                    "viser forandringer i hånd som mulig må opereres. Venter på time. Det er mulig sykmeldeingen vil vare utover aktuell sm periode. "
            }
        }
        meldingTilNav = if (sykmeldingRequest.regelsettVersjon == "3") {
            HelseOpplysningerArbeidsuforhet.MeldingTilNav().apply {
                beskrivBistandNAV = "Trenger bistand"
            }
        } else {
            HelseOpplysningerArbeidsuforhet.MeldingTilNav().apply {
                isBistandNAVUmiddelbart = true
                beskrivBistandNAV = "Trenger bistand"
            }
        }
        meldingTilArbeidsgiver = "Melding til arbeidsgiver"
        kontaktMedPasient = HelseOpplysningerArbeidsuforhet.KontaktMedPasient().apply {
            kontaktDato = sykmeldingRequest.kontaktDato
            begrunnIkkeKontakt = sykmeldingRequest.begrunnIkkeKontakt
            behandletDato = sykmeldingRequest.behandletDato.atStartOfDay()
        }
        utdypendeOpplysninger = if (sykmeldingRequest.utenUtdypendeOpplysninger) {
            null
        } else if (sykmeldingRequest.regelsettVersjon == "3") {
            HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger().apply {
                spmGruppe.addAll(tilSpmGruppeRegelsett3())
            }
        } else {
            HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger().apply {
                spmGruppe.addAll(tilSpmGruppe())
            }
        }
        behandler = HelseOpplysningerArbeidsuforhet.Behandler().apply {
            navn = NavnType().apply {
                fornavn = lege.navn.fornavn
                mellomnavn = lege.navn.mellomnavn
                etternavn = lege.navn.etternavn
            }
            id.addAll(
                listOf(
                    Ident().apply {
                        id = sykmeldingRequest.fnrLege
                        typeId = CV().apply {
                            dn = "Fødselsnummer"
                            s = "2.16.578.1.12.4.1.1.8327"
                            v = "FNR"
                        }
                    }
                )
            )
            adresse = Address()
            kontaktInfo.add(
                TeleCom().apply {
                    typeTelecom = CS().apply {
                        v = "HP"
                        dn = "Hovedtelefon"
                    }
                    teleAddress = URL().apply {
                        v = "tel: 90909090"
                    }
                }
            )
        }
        regelSettVersjon = sykmeldingRequest.regelsettVersjon
        strekkode = "00170272416462604201615322390000011"
        avsenderSystem = HelseOpplysningerArbeidsuforhet.AvsenderSystem().apply {
            systemNavn = "System X"
            systemVersjon = "2015 R1 (4835)"
        }
    }
}

private fun medisinskVurdering(kode: String, system: String, annenFraverGrunn: AnnenFraverGrunn?): HelseOpplysningerArbeidsuforhet.MedisinskVurdering {
    val medisinskVurdering = HelseOpplysningerArbeidsuforhet.MedisinskVurdering().apply {
        hovedDiagnose = HelseOpplysningerArbeidsuforhet.MedisinskVurdering.HovedDiagnose().apply {
            diagnosekode = tilDiagnosekode(kode, system)
        }
        biDiagnoser = HelseOpplysningerArbeidsuforhet.MedisinskVurdering.BiDiagnoser().apply {
            diagnosekode.add(
                CV().apply {
                    s = Diagnosekoder.ICD10_CODE
                    v = "M674"
                    dn = Diagnosekoder.icd10["M674"]?.text ?: ""
                }
            )
        }
        isYrkesskade = false
        yrkesskadeDato = null
        isSvangerskap = false
        isSkjermesForPasient = false
        annenFraversArsak = annenFraverGrunn?.let {
            ArsakType().apply {
                arsakskode.add(
                    CS().apply {
                        v = it.codeValue
                        dn = it.text
                    }
                )
            }
        }
    }
    return medisinskVurdering
}

private fun tilDiagnosekode(kode: String, system: String): CV {
    val diagnosekodesystem = if (system == "icpc2") {
        Diagnosekoder.ICPC2_CODE
    } else {
        Diagnosekoder.ICD10_CODE
    }
    return CV().apply {
        s = diagnosekodesystem
        v = kode
        dn = if (diagnosekodesystem == Diagnosekoder.ICPC2_CODE) {
            Diagnosekoder.icpc2[kode]?.text
        } else {
            Diagnosekoder.icd10[kode]?.text
        } ?: ""
    }
}

private fun tilPeriode(periode: SykmeldingPeriode): HelseOpplysningerArbeidsuforhet.Aktivitet.Periode {
    val xmlperiode = HelseOpplysningerArbeidsuforhet.Aktivitet.Periode().apply {
        periodeFOMDato = periode.fom
        periodeTOMDato = periode.tom
        aktivitetIkkeMulig = if (periode.type == SykmeldingType.HUNDREPROSENT) { hundreprosent() } else null
        avventendeSykmelding = if (periode.type == SykmeldingType.AVVENTENDE) { avventende() } else null
        gradertSykmelding = when (periode.type) {
            SykmeldingType.GRADERT_20 -> {
                gradert(20, false)
            }
            SykmeldingType.GRADERT_40 -> {
                gradert(40, false)
            }
            SykmeldingType.GRADERT_50 -> {
                gradert(50, false)
            }
            SykmeldingType.GRADERT_60 -> {
                gradert(60, false)
            }
            SykmeldingType.GRADERT_80 -> {
                gradert(80, false)
            }
            SykmeldingType.GRADERT_REISETILSKUDD -> {
                gradert(60, true)
            }
            else -> null
        }
        behandlingsdager = when (periode.type) {
            SykmeldingType.BEHANDLINGSDAGER -> {
                behandlingsdager(4)
            }
            SykmeldingType.BEHANDLINGSDAG -> {
                behandlingsdager(1)
            }
            else -> null
        }
        isReisetilskudd = periode.type == SykmeldingType.REISETILSKUDD
    }
    return xmlperiode
}

private fun gradert(grad: Int, reisetilskudd: Boolean): HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.GradertSykmelding {
    return HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.GradertSykmelding().apply {
        sykmeldingsgrad = grad
        isReisetilskudd = reisetilskudd
    }
}

private fun hundreprosent(): HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.AktivitetIkkeMulig {
    return HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.AktivitetIkkeMulig().apply {
        arbeidsplassen = ArsakType().apply {
            beskriv = "andre årsaker til sykefravær"
            arsakskode.add(
                CS().apply {
                    v = "9"
                    dn = "Annet"
                }
            )
        }
        medisinskeArsaker = ArsakType().apply {
            beskriv = "medisinske årsaker til sykefravær"
            arsakskode.add(
                CS().apply {
                    v = "3"
                    dn = "Annet"
                }
            )
        }
    }
}

private fun avventende(): HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.AvventendeSykmelding {
    return HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.AvventendeSykmelding().apply {
        innspillTilArbeidsgiver = "Godt inspill"
    }
}

private fun behandlingsdager(antallBehandlingsdager: Int): HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.Behandlingsdager {
    return HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.Behandlingsdager().apply {
        antallBehandlingsdagerUke = antallBehandlingsdager
    }
}

fun tilSpmGruppe(): List<HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger.SpmGruppe> {
    val listeDynaSvarType = ArrayList<DynaSvarType>()

    listeDynaSvarType.add(
        DynaSvarType().apply {
            spmId = "6.2.1"
            spmTekst = "Beskriv kort sykehistorie, symptomer og funn i dagens situasjon."
            restriksjon = DynaSvarType.Restriksjon().apply {
                restriksjonskode.add(
                    CS().apply {
                        v = "A"
                        dn = "Informasjonen skal ikke vises arbeidsgiver"
                    }
                )
            }
            svarTekst = "Langvarig korsryggsmerter. Ømhet og smerte"
        }
    )
    listeDynaSvarType.add(
        DynaSvarType().apply {
            spmId = "6.2.2"
            spmTekst = "Hvordan påvirker sykdommen arbeidsevnen?"
            restriksjon = DynaSvarType.Restriksjon().apply {
                restriksjonskode.add(
                    CS().apply {
                        v = "A"
                        dn = "Informasjonen skal ikke vises arbeidsgiver"
                    }
                )
            }
            svarTekst = "Kan ikke utføre arbeidsoppgaver 100% som kreves fra yrket. Duplikatbuster: ${UUID.randomUUID()}"
        }
    )
    listeDynaSvarType.add(
        DynaSvarType().apply {
            spmId = "6.2.3"
            spmTekst = "Har behandlingen frem til nå bedret arbeidsevnen?"
            restriksjon = DynaSvarType.Restriksjon().apply {
                restriksjonskode.add(
                    CS().apply {
                        v = "A"
                        dn = "Informasjonen skal ikke vises arbeidsgiver"
                    }
                )
            }
            svarTekst = "Nei"
        }
    )
    listeDynaSvarType.add(
        DynaSvarType().apply {
            spmId = "6.2.4"
            spmTekst = "Beskriv pågående og planlagt henvisning,utredning og/eller behandling."
            restriksjon = DynaSvarType.Restriksjon().apply {
                restriksjonskode.add(
                    CS().apply {
                        v = "A"
                        dn = "Informasjonen skal ikke vises arbeidsgiver"
                    }
                )
            }
            svarTekst = "Henvist til fysio"
        }
    )

    val spmGruppe = listOf(
        HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger.SpmGruppe().apply {
            spmGruppeId = "6.2"
            spmGruppeTekst = "Utdypende opplysninger ved 7/8,17 og 39 uker"
            spmSvar.addAll(listeDynaSvarType)
        }
    )
    return spmGruppe
}

fun tilSpmGruppeRegelsett3(): List<HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger.SpmGruppe> {
    val listeDynaSvarType = ArrayList<DynaSvarType>()

    listeDynaSvarType.add(
        DynaSvarType().apply {
            spmId = "6.5.1"
            spmTekst = "Beskriv kort sykdomsutviklingen, symptomer og funn. Hvordan påvirker helsetilstanden funksjonen i arbeid og dagligliv?"
            restriksjon = DynaSvarType.Restriksjon().apply {
                restriksjonskode.add(
                    CS().apply {
                        v = "A"
                        dn = "Informasjonen skal ikke vises arbeidsgiver"
                    }
                )
            }
            svarTekst = "Har ikke blitt noe bedre. Klarer ikke å jobbe eller drive med aktiviteter"
        }
    )
    listeDynaSvarType.add(
        DynaSvarType().apply {
            spmId = "6.5.2"
            spmTekst = "Beskriv pågående og planlagt utredning og/eller behandling. Lar dette seg kombinere med delvis arbeid?"
            restriksjon = DynaSvarType.Restriksjon().apply {
                restriksjonskode.add(
                    CS().apply {
                        v = "A"
                        dn = "Informasjonen skal ikke vises arbeidsgiver"
                    }
                )
            }
            svarTekst = "Henvist til fysio. Duplikatbuster: ${UUID.randomUUID()}"
        }
    )
    listeDynaSvarType.add(
        DynaSvarType().apply {
            spmId = "6.5.3"
            spmTekst = "Kan arbeidsevnen bedres gjennom medisinsk behandling og/eller arbeidsrelatert aktivitet? I så fall hvordan? Angi tidsperspektiv"
            restriksjon = DynaSvarType.Restriksjon().apply {
                restriksjonskode.add(
                    CS().apply {
                        v = "A"
                        dn = "Informasjonen skal ikke vises arbeidsgiver"
                    }
                )
            }
            svarTekst = "Nei"
        }
    )

    val spmGruppe = listOf(
        HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger.SpmGruppe().apply {
            spmGruppeId = "6.5"
            spmGruppeTekst = "Helseopplysninger til NAVs videre vurdering av oppfølging"
            spmSvar.addAll(listeDynaSvarType)
        }
    )
    return spmGruppe
}

fun convertToXmlGregorianCalendar(dato: LocalDate): XMLGregorianCalendar? {
    return try {
        val gregorianCalendar = GregorianCalendar.from(dato.atStartOfDay(ZoneId.systemDefault()))
        DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar)
    } catch (dce: DatatypeConfigurationException) {
        throw RuntimeException(dce)
    }
}

fun xmlIdentHerid(herid: String): XMLIdent {
    return XMLIdent().apply {
        id = herid
        typeId = XMLCV().apply {
            dn = "HER-id"
            s = "2.16.578.1.12.4.1.1.8116"
            v = "HER"
        }
    }
}

fun xmlIdentHPR(hprNummer: String): XMLIdent {
    return XMLIdent().apply {
        id = hprNummer
        typeId = XMLCV().apply {
            dn = "HPR-nummer"
            s = "2.16.578.1.12.4.1.1.8116"
            v = "HPR"
        }
    }
}
