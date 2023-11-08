package no.nav.syfo.papirsykmelding

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import no.nav.helse.eiFellesformat.XMLEIFellesformat
import no.nav.helse.msgHead.XMLCS
import no.nav.helse.msgHead.XMLCV
import no.nav.helse.msgHead.XMLDocument
import no.nav.helse.msgHead.XMLHealthcareProfessional
import no.nav.helse.msgHead.XMLIdent
import no.nav.helse.msgHead.XMLMsgHead
import no.nav.helse.msgHead.XMLMsgInfo
import no.nav.helse.msgHead.XMLOrganisation
import no.nav.helse.msgHead.XMLReceiver
import no.nav.helse.msgHead.XMLRefDoc
import no.nav.helse.msgHead.XMLSender
import no.nav.helse.papirsykemelding.AktivitetType
import no.nav.helse.papirsykemelding.ArbeidsgiverType
import no.nav.helse.papirsykemelding.MedisinskVurderingType
import no.nav.helse.papirsykemelding.PrognoseType
import no.nav.helse.papirsykemelding.Skanningmetadata
import no.nav.helse.papirsykemelding.UtdypendeOpplysningerType
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
import no.nav.syfo.logger
import no.nav.syfo.sm.Diagnosekoder

fun mapOcrFilTilFellesformat(
    skanningmetadata: Skanningmetadata,
    fnrLege: String,
    hprNummer: String,
    sykmeldingId: String,
    fnr: String,
    journalpostId: String,
): XMLEIFellesformat {
    return XMLEIFellesformat().apply {
        any.add(
            XMLMsgHead().apply {
                msgInfo =
                    XMLMsgInfo().apply {
                        type =
                            XMLCS().apply {
                                dn = "Medisinsk vurdering av arbeidsmulighet ved sykdom, sykmelding"
                                v = "SYKMELD"
                            }
                        miGversion = "v1.2 2006-05-24"
                        genDate =
                            velgRiktigKontaktOgSignaturDato(
                                    skanningmetadata.sykemeldinger.kontaktMedPasient?.behandletDato,
                                    tilPeriodeListe(skanningmetadata.sykemeldinger.aktivitet)
                                )
                                .format(
                                    DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                                )
                        msgId = sykmeldingId
                        ack =
                            XMLCS().apply {
                                dn = "Ja"
                                v = "J"
                            }
                        sender =
                            XMLSender().apply {
                                comMethod =
                                    XMLCS().apply {
                                        dn = "EDI"
                                        v = "EDI"
                                    }
                                organisation =
                                    XMLOrganisation().apply {
                                        healthcareProfessional =
                                            XMLHealthcareProfessional().apply {
                                                givenName = ""
                                                middleName = null
                                                familyName = ""
                                                ident.addAll(
                                                    listOf(
                                                        XMLIdent().apply {
                                                            id = hprNummer
                                                            typeId =
                                                                XMLCV().apply {
                                                                    dn = "HPR-nummer"
                                                                    s =
                                                                        "6.87.654.3.21.9.8.7.6543.2198"
                                                                    v = "HPR"
                                                                }
                                                        },
                                                        XMLIdent().apply {
                                                            id = fnrLege
                                                            typeId =
                                                                XMLCV().apply {
                                                                    dn = "Fødselsnummer"
                                                                    s = "2.16.578.1.12.4.1.1.8327"
                                                                    v = "FNR"
                                                                }
                                                        },
                                                    ),
                                                )
                                            }
                                    }
                            }
                        receiver =
                            XMLReceiver().apply {
                                comMethod =
                                    XMLCS().apply {
                                        dn = "EDI"
                                        v = "EDI"
                                    }
                                organisation =
                                    XMLOrganisation().apply {
                                        organisationName = "NAV"
                                        ident.addAll(
                                            listOf(
                                                XMLIdent().apply {
                                                    id = "79768"
                                                    typeId =
                                                        XMLCV().apply {
                                                            dn =
                                                                "Identifikator fra Helsetjenesteenhetsregisteret (HER-id)"
                                                            s = "2.16.578.1.12.4.1.1.9051"
                                                            v = "HER"
                                                        }
                                                },
                                                XMLIdent().apply {
                                                    id = "889640782"
                                                    typeId =
                                                        XMLCV().apply {
                                                            dn =
                                                                "Organisasjonsnummeret i Enhetsregister (Brønøysund)"
                                                            s = "2.16.578.1.12.4.1.1.9051"
                                                            v = "ENH"
                                                        }
                                                },
                                            ),
                                        )
                                    }
                            }
                    }
                document.add(
                    XMLDocument().apply {
                        refDoc =
                            XMLRefDoc().apply {
                                msgType =
                                    XMLCS().apply {
                                        dn = "XML-instans"
                                        v = "XML"
                                    }
                                content =
                                    XMLRefDoc.Content().apply {
                                        any.add(
                                            HelseOpplysningerArbeidsuforhet().apply {
                                                syketilfelleStartDato =
                                                    velgRiktigSyketilfelleDato(
                                                        skanningmetadata.sykemeldinger
                                                            .syketilfelleStartDato,
                                                        skanningmetadata.sykemeldinger
                                                            .kontaktMedPasient
                                                            ?.behandletDato,
                                                        tilPeriodeListe(
                                                            skanningmetadata.sykemeldinger.aktivitet
                                                        ),
                                                    )
                                                pasient =
                                                    HelseOpplysningerArbeidsuforhet.Pasient()
                                                        .apply {
                                                            navn =
                                                                NavnType().apply {
                                                                    fornavn = ""
                                                                    mellomnavn = null
                                                                    etternavn = ""
                                                                }
                                                            fodselsnummer =
                                                                Ident().apply {
                                                                    id = fnr
                                                                    typeId =
                                                                        CV().apply {
                                                                            dn = "Fødselsnummer"
                                                                            s =
                                                                                "2.16.578.1.12.4.1.1.8116"
                                                                            v = "FNR"
                                                                        }
                                                                }
                                                        }
                                                arbeidsgiver =
                                                    tilArbeidsgiver(
                                                        skanningmetadata.sykemeldinger.arbeidsgiver
                                                    )
                                                medisinskVurdering =
                                                    tilMedisinskVurdering(
                                                        skanningmetadata.sykemeldinger
                                                            .medisinskVurdering
                                                    )
                                                aktivitet =
                                                    HelseOpplysningerArbeidsuforhet.Aktivitet()
                                                        .apply {
                                                            periode.addAll(
                                                                tilPeriodeListe(
                                                                    skanningmetadata.sykemeldinger
                                                                        .aktivitet
                                                                )
                                                            )
                                                        }
                                                prognose =
                                                    skanningmetadata.sykemeldinger.prognose?.let {
                                                        tilPrognose(
                                                            skanningmetadata.sykemeldinger.prognose
                                                        )
                                                    }
                                                utdypendeOpplysninger =
                                                    tilUtdypendeOpplysninger(
                                                        skanningmetadata.sykemeldinger
                                                            .utdypendeOpplysninger
                                                    )
                                                tiltak =
                                                    HelseOpplysningerArbeidsuforhet.Tiltak().apply {
                                                        tiltakArbeidsplassen =
                                                            skanningmetadata.sykemeldinger.tiltak
                                                                ?.tiltakArbeidsplassen
                                                        tiltakNAV =
                                                            skanningmetadata.sykemeldinger.tiltak
                                                                ?.tiltakNAV
                                                        andreTiltak =
                                                            skanningmetadata.sykemeldinger.tiltak
                                                                ?.andreTiltak
                                                    }
                                                meldingTilNav =
                                                    skanningmetadata.sykemeldinger.meldingTilNAV
                                                        ?.let {
                                                            HelseOpplysningerArbeidsuforhet
                                                                .MeldingTilNav()
                                                                .apply {
                                                                    beskrivBistandNAV =
                                                                        skanningmetadata
                                                                            .sykemeldinger
                                                                            .meldingTilNAV
                                                                            ?.beskrivBistandNAV
                                                                    isBistandNAVUmiddelbart =
                                                                        skanningmetadata
                                                                            .sykemeldinger
                                                                            .meldingTilNAV
                                                                            ?.isBistandNAVUmiddelbart
                                                                            ?: false
                                                                }
                                                        }
                                                meldingTilArbeidsgiver =
                                                    skanningmetadata.sykemeldinger
                                                        .meldingTilArbeidsgiver
                                                kontaktMedPasient =
                                                    HelseOpplysningerArbeidsuforhet
                                                        .KontaktMedPasient()
                                                        .apply {
                                                            kontaktDato =
                                                                skanningmetadata.sykemeldinger
                                                                    .tilbakedatering
                                                                    ?.tilbakeDato
                                                            begrunnIkkeKontakt =
                                                                skanningmetadata.sykemeldinger
                                                                    .tilbakedatering
                                                                    ?.tilbakebegrunnelse
                                                            behandletDato =
                                                                velgRiktigKontaktOgSignaturDato(
                                                                    skanningmetadata.sykemeldinger
                                                                        .kontaktMedPasient
                                                                        ?.behandletDato,
                                                                    tilPeriodeListe(
                                                                        skanningmetadata
                                                                            .sykemeldinger
                                                                            .aktivitet
                                                                    )
                                                                )
                                                        }
                                                behandler = tilBehandler(hprNummer, fnrLege)
                                                avsenderSystem =
                                                    HelseOpplysningerArbeidsuforhet.AvsenderSystem()
                                                        .apply {
                                                            systemNavn = "Papirsykmelding"
                                                            systemVersjon =
                                                                journalpostId // Dette er nødvendig
                                                            // for at vi skal
                                                            // slippe å opprette
                                                            // generert PDF for
                                                            // papirsykmeldinger i
                                                            // syfosmsak
                                                        }
                                                strekkode = "123456789qwerty"
                                            },
                                        )
                                    }
                            }
                    },
                )
            },
        )
    }
}

fun tilBehandler(hprNummer: String, fnrLege: String): HelseOpplysningerArbeidsuforhet.Behandler =
    HelseOpplysningerArbeidsuforhet.Behandler().apply {
        navn =
            NavnType().apply {
                fornavn = ""
                mellomnavn = null
                etternavn = ""
            }
        id.addAll(
            listOf(
                Ident().apply {
                    id = hprNummer
                    typeId =
                        CV().apply {
                            dn = "HPR-nummer"
                            s = "6.87.654.3.21.9.8.7.6543.2198"
                            v = "HPR"
                        }
                },
                Ident().apply {
                    id = fnrLege
                    typeId =
                        CV().apply {
                            dn = "Fødselsnummer"
                            s = "2.16.578.1.12.4.1.1.8327"
                            v = "FNR"
                        }
                },
            ),
        )
        adresse = Address()
        kontaktInfo.add(
            TeleCom().apply {
                typeTelecom =
                    CS().apply {
                        v = "HP"
                        dn = "Hovedtelefon"
                    }
                teleAddress = URL().apply { v = null }
            },
        )
    }

fun tilUtdypendeOpplysninger(
    utdypendeOpplysningerType: UtdypendeOpplysningerType?
): HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger {
    val utdypendeOpplysninger =
        HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger().apply {
            spmGruppe.addAll(tilSpmGruppe(utdypendeOpplysningerType))
        }

    return utdypendeOpplysninger
}

fun tilSpmGruppe(
    utdypendeOpplysningerType: UtdypendeOpplysningerType?
): List<HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger.SpmGruppe> {
    val listeDynaSvarType = ArrayList<DynaSvarType>()

    if (utdypendeOpplysningerType?.sykehistorie != null) {
        listeDynaSvarType.add(
            DynaSvarType().apply {
                spmId = "6.2.1"
                spmTekst = "Beskriv kort sykehistorie, symptomer og funn i dagens situasjon."
                restriksjon =
                    DynaSvarType.Restriksjon().apply {
                        restriksjonskode.add(
                            CS().apply {
                                v = "A"
                                dn = "Informasjonen skal ikke vises arbeidsgiver"
                            },
                        )
                    }
                svarTekst = utdypendeOpplysningerType.sykehistorie
            },
        )
    }

    if (utdypendeOpplysningerType?.sykehistorie != null) {
        listeDynaSvarType.add(
            DynaSvarType().apply {
                spmId = "6.2.2"
                spmTekst = "Hvordan påvirker sykdommen arbeidsevnen?"
                restriksjon =
                    DynaSvarType.Restriksjon().apply {
                        restriksjonskode.add(
                            CS().apply {
                                v = "A"
                                dn = "Informasjonen skal ikke vises arbeidsgiver"
                            },
                        )
                    }
                svarTekst = utdypendeOpplysningerType.arbeidsevne
            },
        )
    }

    if (utdypendeOpplysningerType?.sykehistorie != null) {
        listeDynaSvarType.add(
            DynaSvarType().apply {
                spmId = "6.2.3"
                spmTekst = "Har behandlingen frem til nå bedret arbeidsevnen?"
                restriksjon =
                    DynaSvarType.Restriksjon().apply {
                        restriksjonskode.add(
                            CS().apply {
                                v = "A"
                                dn = "Informasjonen skal ikke vises arbeidsgiver"
                            },
                        )
                    }
                svarTekst = utdypendeOpplysningerType.behandlingsresultat
            },
        )
    }

    if (utdypendeOpplysningerType?.sykehistorie != null) {
        listeDynaSvarType.add(
            DynaSvarType().apply {
                spmId = "6.2.4"
                spmTekst = "Beskriv pågående og planlagt henvisning,utredning og/eller behandling."
                restriksjon =
                    DynaSvarType.Restriksjon().apply {
                        restriksjonskode.add(
                            CS().apply {
                                v = "A"
                                dn = "Informasjonen skal ikke vises arbeidsgiver"
                            },
                        )
                    }
                svarTekst = utdypendeOpplysningerType.planlagtBehandling
            },
        )
    }

    // Spørsmålene kommer herfra:
    // https://stash.adeo.no/projects/EIA/repos/nav-eia-external/browse/SM2013/xml/SM2013DynaSpm_1_5.xml
    val spmGruppe =
        listOf(
            HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger.SpmGruppe().apply {
                spmGruppeId = "6.2"
                spmGruppeTekst = "Utdypende opplysninger ved 7/8,17 og 39 uker"
                spmSvar.addAll(listeDynaSvarType)
            },
        )

    if (spmGruppe.first().spmSvar.isNotEmpty()) {
        return spmGruppe
    }

    return ArrayList<HelseOpplysningerArbeidsuforhet.UtdypendeOpplysninger.SpmGruppe>()
}

fun tilPrognose(prognoseType: PrognoseType): HelseOpplysningerArbeidsuforhet.Prognose =
    HelseOpplysningerArbeidsuforhet.Prognose().apply {
        isArbeidsforEtterEndtPeriode =
            prognoseType.friskmelding?.isArbeidsforEtterEndtPeriode ?: false
        beskrivHensynArbeidsplassen = prognoseType.friskmelding?.beskrivHensynArbeidsplassen
        erIArbeid =
            prognoseType.medArbeidsgiver?.let {
                HelseOpplysningerArbeidsuforhet.Prognose.ErIArbeid().apply {
                    isEgetArbeidPaSikt = it.isTilbakeSammeArbeidsgiver
                    isAnnetArbeidPaSikt = it.isTilbakeAnnenArbeidsgiver
                    arbeidFraDato = it.tilbakeDato
                    vurderingDato = it.datoNyTilbakemelding
                }
            }
        erIkkeIArbeid =
            prognoseType.utenArbeidsgiver?.let {
                HelseOpplysningerArbeidsuforhet.Prognose.ErIkkeIArbeid().apply {
                    isArbeidsforPaSikt = it.isTilbakeArbeid
                    arbeidsforFraDato = it.tilbakeDato
                    vurderingDato = it.datoNyTilbakemelding
                }
            }
    }

fun tilPeriodeListe(
    aktivitetType: AktivitetType
): List<HelseOpplysningerArbeidsuforhet.Aktivitet.Periode> {
    val periodeListe = ArrayList<HelseOpplysningerArbeidsuforhet.Aktivitet.Periode>()

    if (aktivitetType.aktivitetIkkeMulig != null) {
        periodeListe.add(
            HelseOpplysningerArbeidsuforhet.Aktivitet.Periode().apply {
                periodeFOMDato = aktivitetType.aktivitetIkkeMulig.periodeFOMDato
                periodeTOMDato = aktivitetType.aktivitetIkkeMulig.periodeTOMDato
                aktivitetIkkeMulig =
                    HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.AktivitetIkkeMulig().apply {
                        medisinskeArsaker =
                            if (aktivitetType.aktivitetIkkeMulig.medisinskeArsaker != null) {
                                ArsakType().apply {
                                    beskriv =
                                        aktivitetType.aktivitetIkkeMulig.medisinskeArsaker
                                            .medArsakerBesk
                                    arsakskode.add(CS())
                                }
                            } else {
                                null
                            }
                        arbeidsplassen =
                            if (aktivitetType.aktivitetIkkeMulig.arbeidsplassen != null) {
                                ArsakType().apply {
                                    beskriv =
                                        aktivitetType.aktivitetIkkeMulig.arbeidsplassen
                                            .arbeidsplassenBesk
                                    arsakskode.add(CS())
                                }
                            } else {
                                null
                            }
                    }
                avventendeSykmelding = null
                gradertSykmelding = null
                behandlingsdager = null
                isReisetilskudd = false
            },
        )
    }

    if (aktivitetType.gradertSykmelding != null) {
        periodeListe.add(
            HelseOpplysningerArbeidsuforhet.Aktivitet.Periode().apply {
                periodeFOMDato = aktivitetType.gradertSykmelding.periodeFOMDato
                periodeTOMDato = aktivitetType.gradertSykmelding.periodeTOMDato
                aktivitetIkkeMulig = null
                avventendeSykmelding = null
                gradertSykmelding =
                    HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.GradertSykmelding().apply {
                        isReisetilskudd = aktivitetType.gradertSykmelding.isReisetilskudd ?: false
                        sykmeldingsgrad =
                            Integer.valueOf(aktivitetType.gradertSykmelding.sykmeldingsgrad)
                    }
                behandlingsdager = null
                isReisetilskudd = false
            },
        )
    }
    if (
        aktivitetType.avventendeSykmelding != null &&
            !aktivitetType.innspillTilArbeidsgiver.isNullOrEmpty()
    ) {
        periodeListe.add(
            HelseOpplysningerArbeidsuforhet.Aktivitet.Periode().apply {
                periodeFOMDato = aktivitetType.avventendeSykmelding.periodeFOMDato
                periodeTOMDato = aktivitetType.avventendeSykmelding.periodeTOMDato
                aktivitetIkkeMulig = null
                avventendeSykmelding =
                    HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.AvventendeSykmelding().apply {
                        innspillTilArbeidsgiver = aktivitetType.innspillTilArbeidsgiver
                    }
                gradertSykmelding = null
                behandlingsdager = null
                isReisetilskudd = false
            },
        )
    }
    if (aktivitetType.behandlingsdager != null) {
        periodeListe.add(
            HelseOpplysningerArbeidsuforhet.Aktivitet.Periode().apply {
                periodeFOMDato = aktivitetType.behandlingsdager.periodeFOMDato
                periodeTOMDato = aktivitetType.behandlingsdager.periodeTOMDato
                aktivitetIkkeMulig = null
                avventendeSykmelding = null
                gradertSykmelding = null
                behandlingsdager =
                    HelseOpplysningerArbeidsuforhet.Aktivitet.Periode.Behandlingsdager().apply {
                        antallBehandlingsdagerUke =
                            aktivitetType.behandlingsdager?.antallBehandlingsdager?.toInt() ?: 1
                    }
                isReisetilskudd = false
            },
        )
    }
    if (aktivitetType.reisetilskudd != null) {
        periodeListe.add(
            HelseOpplysningerArbeidsuforhet.Aktivitet.Periode().apply {
                periodeFOMDato = aktivitetType.reisetilskudd.periodeFOMDato
                periodeTOMDato = aktivitetType.reisetilskudd.periodeTOMDato
                aktivitetIkkeMulig = null
                avventendeSykmelding = null
                gradertSykmelding = null
                behandlingsdager = null
                isReisetilskudd = true
            },
        )
    }
    if (periodeListe.isEmpty()) {
        logger.warn("Could not find aktivitetstype")
        throw IllegalStateException("Cound not find aktivitetstype")
    }
    return periodeListe
}

fun tilArbeidsgiver(
    arbeidsgiverType: ArbeidsgiverType?
): HelseOpplysningerArbeidsuforhet.Arbeidsgiver =
    HelseOpplysningerArbeidsuforhet.Arbeidsgiver().apply {
        harArbeidsgiver =
            with(arbeidsgiverType?.harArbeidsgiver?.lowercase()) {
                when {
                    this == null ->
                        CS().apply {
                            dn = "Ingen arbeidsgiver"
                            v = "3"
                        }
                    this.contains("ingen") ->
                        CS().apply {
                            dn = "Ingen arbeidsgiver"
                            v = "3"
                        }
                    this.contains("flere") ->
                        CS().apply {
                            dn = "Flere arbeidsgivere"
                            v = "2"
                        }
                    this.contains("en") ->
                        CS().apply {
                            dn = "Én arbeidsgiver"
                            v = "1"
                        }
                    this.isNotBlank() ->
                        CS().apply {
                            dn = "Én arbeidsgiver"
                            v = "1"
                        }
                    else -> {
                        logger.warn(
                            "Klarte ikke å mappe {} til riktig harArbeidsgiver-verdi, bruker en arbeidsgiver som standard",
                            arbeidsgiverType?.harArbeidsgiver
                        )
                        CS().apply {
                            dn = "Ingen arbeidsgiver"
                            v = "3"
                        }
                    }
                }
            }
        navnArbeidsgiver = arbeidsgiverType?.navnArbeidsgiver
        yrkesbetegnelse = arbeidsgiverType?.yrkesbetegnelse
        stillingsprosent = arbeidsgiverType?.stillingsprosent?.toInt()
    }

fun tilMedisinskVurdering(
    medisinskVurderingType: MedisinskVurderingType
): HelseOpplysningerArbeidsuforhet.MedisinskVurdering {
    if (
        medisinskVurderingType.hovedDiagnose.isNullOrEmpty() &&
            medisinskVurderingType.annenFraversArsak.isNullOrEmpty()
    ) {
        logger.warn("Sykmelding mangler hoveddiagnose og annenFraversArsak, avbryter..")
        throw IllegalStateException("Sykmelding mangler hoveddiagnose")
    }

    val biDiagnoseListe: List<CV>? =
        medisinskVurderingType.bidiagnose?.map {
            toMedisinskVurderingDiagnose(it.diagnosekode, it.diagnosekodeSystem, it.diagnose)
        }

    return HelseOpplysningerArbeidsuforhet.MedisinskVurdering().apply {
        if (!medisinskVurderingType.hovedDiagnose.isNullOrEmpty()) {
            hovedDiagnose =
                HelseOpplysningerArbeidsuforhet.MedisinskVurdering.HovedDiagnose().apply {
                    diagnosekode =
                        toMedisinskVurderingDiagnose(
                            medisinskVurderingType.hovedDiagnose[0].diagnosekode,
                            medisinskVurderingType.hovedDiagnose[0].diagnosekodeSystem,
                            medisinskVurderingType.hovedDiagnose[0].diagnose
                        )
                }
        }
        if (biDiagnoseListe != null && biDiagnoseListe.isNotEmpty()) {
            biDiagnoser =
                HelseOpplysningerArbeidsuforhet.MedisinskVurdering.BiDiagnoser().apply {
                    diagnosekode.addAll(biDiagnoseListe)
                }
        }
        isSkjermesForPasient = medisinskVurderingType.isSkjermesForPasient
        annenFraversArsak =
            medisinskVurderingType.annenFraversArsak?.let {
                ArsakType().apply {
                    arsakskode.add(CS())
                    beskriv = medisinskVurderingType.annenFraversArsak
                }
            }
        isSvangerskap = medisinskVurderingType.isSvangerskap
        isYrkesskade = medisinskVurderingType.isYrkesskade
        yrkesskadeDato = medisinskVurderingType.yrkesskadedato
    }
}

fun identifiserDiagnoseKodeverk(diagnoseKode: String, system: String?, diagnose: String?): String {
    val sanitisertSystem = system?.replace(".", "")?.replace(" ", "")?.replace("-", "")?.uppercase()
    val sanitisertKode = diagnoseKode.replace(".", "").replace(" ", "").uppercase()

    return if (sanitisertSystem == "ICD10") {
        Diagnosekoder.ICD10_CODE
    } else if (sanitisertSystem == "ICPC2") {
        Diagnosekoder.ICPC2_CODE
    } else if (
        Diagnosekoder.icd10.containsKey(sanitisertKode) &&
            Diagnosekoder.icd10[sanitisertKode]?.text == diagnose
    ) {
        Diagnosekoder.ICD10_CODE
    } else if (
        Diagnosekoder.icpc2.containsKey(sanitisertKode) &&
            Diagnosekoder.icpc2[sanitisertKode]?.text == diagnose
    ) {
        Diagnosekoder.ICPC2_CODE
    } else {
        ""
    }
}

fun toMedisinskVurderingDiagnose(
    originalDiagnosekode: String,
    originalSystem: String?,
    diagnose: String?
): CV {
    val diagnosekode =
        if (originalDiagnosekode.contains(".")) {
            originalDiagnosekode.replace(".", "").uppercase().replace(" ", "")
        } else {
            originalDiagnosekode.uppercase().replace(" ", "")
        }

    val identifisertKodeverk =
        identifiserDiagnoseKodeverk(originalDiagnosekode, originalSystem, diagnose)

    when {
        identifisertKodeverk == Diagnosekoder.ICD10_CODE &&
            Diagnosekoder.icd10.containsKey(diagnosekode) -> {
            logger.info(
                "Mappet $originalDiagnosekode til $diagnosekode for ICD10, basert på angitt diagnosekode og kodeverk/diagnosetekst"
            )
            return CV().apply {
                s = Diagnosekoder.ICD10_CODE
                v = diagnosekode
                dn = Diagnosekoder.icd10[diagnosekode]?.text ?: ""
            }
        }
        identifisertKodeverk == Diagnosekoder.ICPC2_CODE &&
            Diagnosekoder.icpc2.containsKey(diagnosekode) -> {
            logger.info(
                "Mappet $originalDiagnosekode til $diagnosekode for ICPC2, basert på angitt diagnosekode og kodeverk/diagnosetekst"
            )
            return CV().apply {
                s = Diagnosekoder.ICPC2_CODE
                v = diagnosekode
                dn = Diagnosekoder.icpc2[diagnosekode]?.text ?: ""
            }
        }
        identifisertKodeverk.isEmpty() &&
            Diagnosekoder.icd10.containsKey(diagnosekode) &&
            !Diagnosekoder.icpc2.containsKey(diagnosekode) -> {
            logger.info(
                "Mappet $originalDiagnosekode til $diagnosekode for ICD10, basert på angitt diagnosekode (kodeverk ikke angitt)"
            )
            return CV().apply {
                s = Diagnosekoder.ICD10_CODE
                v = diagnosekode
                dn = Diagnosekoder.icd10[diagnosekode]?.text ?: ""
            }
        }
        identifisertKodeverk.isEmpty() &&
            Diagnosekoder.icpc2.containsKey(diagnosekode) &&
            !Diagnosekoder.icd10.containsKey(diagnosekode) -> {
            logger.info(
                "Mappet $originalDiagnosekode til $diagnosekode for ICPC2, basert på angitt diagnosekode (kodeverk ikke angitt)"
            )
            return CV().apply {
                s = Diagnosekoder.ICPC2_CODE
                v = diagnosekode
                dn = Diagnosekoder.icpc2[diagnosekode]?.text ?: ""
            }
        }
        else -> {
            logger.warn("Diagnosekode $originalDiagnosekode tilhører ingen kjente kodeverk")
            throw IllegalStateException(
                "Diagnosekode $originalDiagnosekode tilhører ingen kjente kodeverk"
            )
        }
    }
}

fun velgRiktigKontaktOgSignaturDato(
    behandletDato: LocalDate?,
    periodeliste: List<HelseOpplysningerArbeidsuforhet.Aktivitet.Periode>
): LocalDateTime {
    behandletDato?.let {
        return LocalDateTime.of(it, LocalTime.NOON)
    }

    if (periodeliste.isEmpty()) {
        logger.warn("Periodeliste er tom, kan ikke fortsette")
        throw IllegalStateException("Periodeliste er tom, kan ikke fortsette")
    }
    if (periodeliste.size > 1) {
        logger.info("Periodeliste inneholder mer enn en periode")
    }

    periodeliste.forEach {
        if (it.aktivitetIkkeMulig != null) {
            return LocalDateTime.of(it.periodeFOMDato, LocalTime.NOON)
        }
    }
    logger.info("Periodeliste mangler aktivitetIkkeMulig, bruker FOM fra første periode")
    return LocalDateTime.of(periodeliste.first().periodeFOMDato, LocalTime.NOON)
}

fun velgRiktigSyketilfelleDato(
    syketilfelledato: LocalDate?,
    behandletDato: LocalDate?,
    periodeliste: List<HelseOpplysningerArbeidsuforhet.Aktivitet.Periode>,
): LocalDate {
    syketilfelledato?.let {
        return syketilfelledato
    }

    behandletDato?.let {
        return behandletDato
    }

    if (periodeliste.isNotEmpty() && periodeliste.size > 1) {
        periodeliste.forEach {
            if (it.aktivitetIkkeMulig != null) {
                return it.periodeFOMDato
            }
        }
    }

    logger.info("Periodeliste mangler aktivitetIkkeMulig, bruker FOM fra første periode")
    return periodeliste.first().periodeFOMDato
}
