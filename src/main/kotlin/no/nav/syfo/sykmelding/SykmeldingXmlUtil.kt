package no.nav.syfo.sykmelding

import no.nav.helse.msgHead.XMLCV
import no.nav.helse.msgHead.XMLIdent
import no.nav.helse.sm2013.Address
import no.nav.helse.sm2013.ArsakType
import no.nav.helse.sm2013.CS
import no.nav.helse.sm2013.CV
import no.nav.helse.sm2013.DynaSvarType
import no.nav.helse.sm2013.HelseOpplysningerArbeidsuforhet
import no.nav.helse.sm2013.HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger.SpmGruppe
import no.nav.helse.sm2013.Ident
import no.nav.helse.sm2013.NavnType
import no.nav.helse.sm2013.TeleCom
import no.nav.helse.sm2013.URL
import no.nav.syfo.model.SykmeldingPeriode
import no.nav.syfo.model.SykmeldingType
import no.nav.syfo.pdl.model.PdlPerson
import no.nav.syfo.sm.Diagnosekoder
import no.nav.syfo.sykmelding.model.Diagnoser
import no.nav.syfo.sykmelding.model.SykmeldingRequest
import no.nav.syfo.sykmelding.model.UtdypendeOpplysninger
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
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
        if (sykmeldingRequest.arbeidsgiverNavn.isNullOrEmpty()) {
            arbeidsgiver = HelseOpplysningerArbeidsuforhet.Arbeidsgiver().apply {
                harArbeidsgiver = CS().apply {
                    dn = "Én arbeidsgiver"
                    v = "1"
                }
                navnArbeidsgiver = "LOMMEN BARNEHAVE"
                yrkesbetegnelse = "Pedagogisk leder"
                stillingsprosent = 100
            }
        } else {
            arbeidsgiver = HelseOpplysningerArbeidsuforhet.Arbeidsgiver().apply {
                harArbeidsgiver = CS().apply {
                    dn = "Én arbeidsgiver"
                    v = "1"
                }
                navnArbeidsgiver = sykmeldingRequest.arbeidsgiverNavn
                yrkesbetegnelse = "Utvikler"
                stillingsprosent = 100
            }
        }
        medisinskVurdering = medisinskVurdering(sykmeldingRequest)
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
        if (!sykmeldingRequest.meldingTilArbeidsgiver.isNullOrEmpty()) {
            meldingTilArbeidsgiver = sykmeldingRequest.meldingTilArbeidsgiver
        }
        kontaktMedPasient = HelseOpplysningerArbeidsuforhet.KontaktMedPasient().apply {
            kontaktDato = sykmeldingRequest.kontaktDato
            begrunnIkkeKontakt = sykmeldingRequest.begrunnIkkeKontakt
            behandletDato = sykmeldingRequest.behandletDato.atStartOfDay()
        }
        utdypendeOpplysninger = if (sykmeldingRequest.utdypendeOpplysninger == UtdypendeOpplysninger.INGEN) {
            null
        } else if (sykmeldingRequest.regelsettVersjon == "3") {
            HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger().apply {
                spmGruppe.addAll(tilSpmGruppeRegelsett3(sykmeldingRequest.utdypendeOpplysninger))
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
                    },
                    Ident().apply {
                        id = sykmeldingRequest.hprNummer
                        typeId = CV().apply {
                            dn = "HPR-nummer"
                            s = "2.16.578.1.12.4.1.1.8116"
                            v = "HPR"
                        }
                    },
                ),
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
                },
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

private fun medisinskVurdering(sykmeldingRequest: SykmeldingRequest): HelseOpplysningerArbeidsuforhet.MedisinskVurdering {
    val medisinskVurdering = HelseOpplysningerArbeidsuforhet.MedisinskVurdering().apply {
        hovedDiagnose = HelseOpplysningerArbeidsuforhet.MedisinskVurdering.HovedDiagnose().apply {
            diagnosekode = tilDiagnosekode(sykmeldingRequest.diagnosekode, sykmeldingRequest.diagnosekodesystem)
        }
        if (!sykmeldingRequest.bidiagnoser.isNullOrEmpty()) {
            biDiagnoser = HelseOpplysningerArbeidsuforhet.MedisinskVurdering.BiDiagnoser().apply {
                diagnosekode.addAll(sykmeldingRequest.bidiagnoser.map { tilDiagnosekode(it) })
            }
        }
        isYrkesskade = sykmeldingRequest.yrkesskade
        yrkesskadeDato = null
        isSvangerskap = false
        isSkjermesForPasient = false
        annenFraversArsak = sykmeldingRequest.annenFraverGrunn?.let {
            ArsakType().apply {
                arsakskode.add(
                    CS().apply {
                        v = it.codeValue
                        dn = it.text
                    },
                )
            }
        }
    }
    return medisinskVurdering
}

private fun tilDiagnosekode(bidiagnoser: Diagnoser): CV {
    val diagnosekodesystem = if (bidiagnoser.code == "icpc2") {
        Diagnosekoder.ICPC2_CODE
    } else {
        Diagnosekoder.ICD10_CODE
    }
    return CV().apply {
        s = bidiagnoser.system
        v = bidiagnoser.code
        dn = if (diagnosekodesystem == Diagnosekoder.ICPC2_CODE) {
            Diagnosekoder.icpc2[bidiagnoser.code]?.text
        } else {
            Diagnosekoder.icd10[bidiagnoser.code]?.text
        } ?: ""
    }
}

private fun tilDiagnosekode(kode: String, system: String): CV {
    val diagnosekodesystem = if (system == "ICPC2") {
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
                behandlingsdager(finnAntallBehandlingsdager(periode.fom, periode.tom) + 1)
            }
            SykmeldingType.BEHANDLINGSDAG -> {
                behandlingsdager(finnAntallBehandlingsdager(periode.fom, periode.tom))
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
                },
            )
        }
        medisinskeArsaker = ArsakType().apply {
            beskriv = "medisinske årsaker til sykefravær"
            arsakskode.add(
                CS().apply {
                    v = "3"
                    dn = "Annet"
                },
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

private fun finnAntallBehandlingsdager(fom: LocalDate, tom: LocalDate): Int {
    val range = fom.rangeTo(tom)
    return ChronoUnit.WEEKS.between(range.start, range.endInclusive).toInt() + 1
}

fun tilSpmGruppe(): List<SpmGruppe> {
    val listeDynaSvarType = ArrayList<DynaSvarType>()

    listeDynaSvarType.add(
        DynaSvarType().apply {
            spmId = "6.4.1"
            spmTekst = "Beskriv kort sykehistorie, symptomer og funn i dagens situasjon."
            restriksjon = DynaSvarType.Restriksjon().apply {
                restriksjonskode.add(
                    CS().apply {
                        v = "A"
                        dn = "Informasjonen skal ikke vises arbeidsgiver"
                    },
                )
            }
            svarTekst = "Langvarig korsryggsmerter. Ømhet og smerte"
        },
    )
    listeDynaSvarType.add(
        DynaSvarType().apply {
            spmId = "6.4.2"
            spmTekst = "Beskriv pågående og planlagt henvisning, utredning og/eller behandling. Lar dette seg kombinere med delvis arbeid?"
            restriksjon = DynaSvarType.Restriksjon().apply {
                restriksjonskode.add(
                    CS().apply {
                        v = "A"
                        dn = "Informasjonen skal ikke vises arbeidsgiver"
                    },
                )
            }
            svarTekst = "Kan ikke utføre arbeidsoppgaver 100% som kreves fra yrket. Duplikatbuster: ${UUID.randomUUID()}"
        },
    )
    listeDynaSvarType.add(
        DynaSvarType().apply {
            spmId = "6.4.3"
            spmTekst = "Hva mener du skal til for at pasienten kan komme tilbake i eget eller annet arbeid?"
            restriksjon = DynaSvarType.Restriksjon().apply {
                restriksjonskode.add(
                    CS().apply {
                        v = "A"
                        dn = "Informasjonen skal ikke vises arbeidsgiver"
                    },
                )
            }
            svarTekst = "Videre utredning"
        },
    )

    val spmGruppe = listOf(
        SpmGruppe().apply {
            spmGruppeId = "6.4"
            spmGruppeTekst = "Helseopplysninger til NAVs videre vurdering av oppfølging"
            spmSvar.addAll(listeDynaSvarType)
        },
    )
    return spmGruppe
}

fun tilSpmGruppeRegelsett3(utdypendeOpplysninger: UtdypendeOpplysninger?): List<SpmGruppe> {
    val listeDynaSvarType = ArrayList<DynaSvarType>()
    val spmGruppe = ArrayList<SpmGruppe>()

    if (utdypendeOpplysninger == UtdypendeOpplysninger.UKE_7) {
        listeDynaSvarType.add(
            DynaSvarType().apply {
                spmId = "6.3.1"
                spmTekst =
                    "Beskriv kort sykehistorie, symptomer og funn. Hvordan påvirker helsetilstanden funksjonen i arbeid og dagligliv?"
                restriksjon = DynaSvarType.Restriksjon().apply {
                    restriksjonskode.add(
                        CS().apply {
                            v = "A"
                            dn = "Informasjonen skal ikke vises arbeidsgiver"
                        },
                    )
                }
                svarTekst = "Har vært syk i 7 uker. Sår hals og vondt i hodet."
            },
        )
        listeDynaSvarType.add(
            DynaSvarType().apply {
                spmId = "6.3.2"
                spmTekst =
                    "Beskriv pågående og planlagt utredning og/eller behandling. Lar dette seg kombinere med delvis arbeid?"
                restriksjon = DynaSvarType.Restriksjon().apply {
                    restriksjonskode.add(
                        CS().apply {
                            v = "A"
                            dn = "Informasjonen skal ikke vises arbeidsgiver"
                        },
                    )
                }
                svarTekst = "Henvist til fysio. Duplikatbuster: ${UUID.randomUUID()}"
            },
        )
        spmGruppe.add(
            SpmGruppe().apply {
                spmGruppeId = "6.3"
                spmGruppeTekst = "Helseopplysninger til vurdering av aktivitetskravet og NAVs oppfølging"
                spmSvar.addAll(listeDynaSvarType)
            },
        )
    } else if (utdypendeOpplysninger == UtdypendeOpplysninger.UKE_17) {
        listeDynaSvarType.add(
            DynaSvarType().apply {
                spmId = "6.4.1"
                spmTekst =
                    "Beskriv kort sykdomsutviklingen, symptomer og funn. Hvordan påvirker helsetilstanden funksjonen i arbeid og dagligliv?"
                restriksjon = DynaSvarType.Restriksjon().apply {
                    restriksjonskode.add(
                        CS().apply {
                            v = "A"
                            dn = "Informasjonen skal ikke vises arbeidsgiver"
                        },
                    )
                }
                svarTekst = "Blir gradvis bedre, men er fortsatt ikke frisk."
            },
        )
        listeDynaSvarType.add(
            DynaSvarType().apply {
                spmId = "6.4.2"
                spmTekst =
                    "Beskriv pågående og planlagt utredning og/eller behandling. Lar dette seg kombinere med delvis arbeid?"
                restriksjon = DynaSvarType.Restriksjon().apply {
                    restriksjonskode.add(
                        CS().apply {
                            v = "A"
                            dn = "Informasjonen skal ikke vises arbeidsgiver"
                        },
                    )
                }
                svarTekst = "Henvist til fysio. Duplikatbuster: ${UUID.randomUUID()}"
            },
        )
        listeDynaSvarType.add(
            DynaSvarType().apply {
                spmId = "6.4.3"
                spmTekst =
                    "Hva mener du skal til for at pasienten kan komme tilbake i eget eller annet arbeid?"
                restriksjon = DynaSvarType.Restriksjon().apply {
                    restriksjonskode.add(
                        CS().apply {
                            v = "A"
                            dn = "Informasjonen skal ikke vises arbeidsgiver"
                        },
                    )
                }
                svarTekst = "Må fullføre behandlingen."
            },
        )
        spmGruppe.add(
            SpmGruppe().apply {
                spmGruppeId = "6.4"
                spmGruppeTekst = "Helseopplysninger til NAVs videre vurdering av oppfølging"
                spmSvar.addAll(listeDynaSvarType)
            },
        )
    } else {
        listeDynaSvarType.add(
            DynaSvarType().apply {
                spmId = "6.5.1"
                spmTekst =
                    "Beskriv kort sykdomsutviklingen, symptomer og funn. Hvordan påvirker helsetilstanden funksjonen i arbeid og dagligliv?"
                restriksjon = DynaSvarType.Restriksjon().apply {
                    restriksjonskode.add(
                        CS().apply {
                            v = "A"
                            dn = "Informasjonen skal ikke vises arbeidsgiver"
                        },
                    )
                }
                svarTekst = "Har ikke blitt noe bedre. Klarer ikke å jobbe eller drive med aktiviteter"
            },
        )
        listeDynaSvarType.add(
            DynaSvarType().apply {
                spmId = "6.5.2"
                spmTekst =
                    "Beskriv pågående og planlagt utredning og/eller behandling. Lar dette seg kombinere med delvis arbeid?"
                restriksjon = DynaSvarType.Restriksjon().apply {
                    restriksjonskode.add(
                        CS().apply {
                            v = "A"
                            dn = "Informasjonen skal ikke vises arbeidsgiver"
                        },
                    )
                }
                svarTekst = "Henvist til fysio. Duplikatbuster: ${UUID.randomUUID()}"
            },
        )
        listeDynaSvarType.add(
            DynaSvarType().apply {
                spmId = "6.5.3"
                spmTekst =
                    "Kan arbeidsevnen bedres gjennom medisinsk behandling og/eller arbeidsrelatert aktivitet? I så fall hvordan? Angi tidsperspektiv"
                restriksjon = DynaSvarType.Restriksjon().apply {
                    restriksjonskode.add(
                        CS().apply {
                            v = "A"
                            dn = "Informasjonen skal ikke vises arbeidsgiver"
                        },
                    )
                }
                svarTekst = "Nei"
            },
        )
        spmGruppe.add(
            SpmGruppe().apply {
                spmGruppeId = "6.5"
                spmGruppeTekst = "Helseopplysninger til NAVs videre vurdering av oppfølging"
                spmSvar.addAll(listeDynaSvarType)
            },
        )
    }
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
