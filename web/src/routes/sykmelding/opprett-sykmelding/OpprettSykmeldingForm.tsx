import { BodyShort, Button, Checkbox, Label, Select, TextField } from '@navikt/ds-react'
import { ReactElement } from 'react'
import { FormProvider, useForm, useFieldArray } from 'react-hook-form'
import { format, sub } from 'date-fns'
import { Heading } from '@navikt/ds-react'

import styles from './OpprettSykmelding.module.css'
import SyketilfelleStartdato from './SyketilfelleStartdato'
import Behandletdato from './Behandletdato'
import Kontaktdato from './Kontaktdato'
import Scenarios from './Scenarios'
import FnrTextField from '../../../components/form/FnrTextField.tsx'
import PeriodePicker from '../../../components/form/PeriodePicker/PeriodePicker.tsx'
import { Periode, SykmeldingType } from '../../../types/sykmelding/Periode.ts'
import DiagnosePicker, { Diagnose } from '../../../components/form/DiagnosePicker/DiagnosePicker.tsx'
import { useMutation } from '@tanstack/react-query'
import { post, SimpleMessage } from '../../../api/fetcher.ts'
import FormPage from '../../../components/layout/FormPage.tsx'

export interface SykmeldingFormValues {
    fnr: string
    fnrLege: string
    herId: string | null
    beskrivBistandNav: string | null
    meldingTilArbeidsgiver: string | null
    hprNummer: string
    syketilfelleStartdato: string
    annenFraverGrunn: string | null
    perioder: Periode[]
    behandletDato: string
    kontaktDato: string | null
    begrunnIkkeKontakt: string | null
    vedlegg: boolean
    virksomhetsykmelding: boolean
    utdypendeOpplysninger: string | null
    regelsettVersjon: string | null
    hoveddiagnose: Diagnose
    bidiagnoser: [Diagnose] | null
    arbeidsgiverNavn: string | null
    vedleggMedVirus: boolean
    yrkesskade: boolean
}

function OpprettSykmeldingForm(): ReactElement {
    const date = new Date()
    const iGar = format(sub(date, { days: 1 }), 'yyyy-MM-dd')
    const enUkeSiden = format(sub(date, { days: 7 }), 'yyyy-MM-dd')
    const form = useForm<SykmeldingFormValues>({
        defaultValues: {
            syketilfelleStartdato: enUkeSiden,
            behandletDato: enUkeSiden,
            perioder: [{ fom: enUkeSiden, tom: iGar, type: SykmeldingType.Enum.HUNDREPROSENT }],
            hoveddiagnose: {
                system: 'icd10',
                code: 'H100',
                text: 'Mukopurulent konjunktivitt',
            },
            arbeidsgiverNavn: 'Eksempel Arbeidsgiversen AS',
        },
    })
    const {
        fields: periodeFields,
        append: perioderAppend,
        remove: perioderRemove,
    } = useFieldArray({
        control: form.control,
        name: 'perioder',
    })

    const {
        append: bidiagnoserAppend,
        remove: bidiagnoserRemove,
        fields: bidiagnoserFields,
    } = useFieldArray({
        control: form.control,
        name: 'bidiagnoser',
    })

    const opprettSykmeldingMutation = useMutation({
        mutationFn: post<SykmeldingFormValues, SimpleMessage>('/sykmelding/opprett'),
    })
    const sjekkRegelMutation = useMutation({
        mutationFn: post<SykmeldingFormValues, unknown>('/sykmelding/regelsjekk'),
    })

    return (
        <FormProvider {...form}>
            <form
                onSubmit={form.handleSubmit((values) => {
                    sjekkRegelMutation.reset()
                    opprettSykmeldingMutation.mutate(mapFormValues(values))
                })}
            >
                <FormPage
                    title="Opprett ny nasjonal sykmelding"
                    mutations={[opprettSykmeldingMutation, sjekkRegelMutation]}
                >
                    <Scenarios />
                    <Heading size="medium" level="3" spacing className="mt-8">
                        Sykmeldingen
                    </Heading>

                    <FnrTextField
                        className={styles.commonFormElement}
                        {...form.register('fnr', { required: true })}
                        label="Fødselsnummer"
                        error={form.formState.errors.fnr && 'Fødselsnummer for pasienten mangler'}
                    />
                    <div className={styles.periodeFields}>
                        {periodeFields.map((it, index) => (
                            <div key={it.id} className={styles.periodeFieldRow}>
                                <PeriodePicker name={`perioder.${index}`} />
                                <Select {...form.register(`perioder.${index}.type`)} label="Sykmeldingstype">
                                    <option value="HUNDREPROSENT">HUNDREPROSENT</option>
                                    <option value="AVVENTENDE">AVVENTENDE</option>
                                    <option value="GRADERT_UNDER_20">GRADERT_UNDER_20</option>
                                    <option value="GRADERT_20">GRADERT_20</option>
                                    <option value="GRADERT_40">GRADERT_40</option>
                                    <option value="GRADERT_50">GRADERT_50</option>
                                    <option value="GRADERT_60">GRADERT_60</option>
                                    <option value="GRADERT_80">GRADERT_80</option>
                                    <option value="GRADERT_95">GRADERT_95</option>
                                    <option value="GRADERT_100">GRADERT_100</option>
                                    <option value="GRADERT_REISETILSKUDD">GRADERT_REISETILSKUDD</option>
                                    <option value="BEHANDLINGSDAGER">BEHANDLINGSDAGER</option>
                                    <option value="BEHANDLINGSDAG">BEHANDLINGSDAG</option>
                                    <option value="REISETILSKUDD">REISETILSKUDD</option>
                                </Select>
                                <Button type="button" onClick={() => perioderRemove(index)} variant="tertiary">
                                    Slett
                                </Button>
                            </div>
                        ))}
                    </div>
                    <div className={styles.periodeButton}>
                        <Button
                            type="button"
                            onClick={() =>
                                perioderAppend({
                                    fom: enUkeSiden,
                                    tom: iGar,
                                    type: SykmeldingType.Enum.HUNDREPROSENT,
                                })
                            }
                        >
                            Legg til periode
                        </Button>
                    </div>
                    <TextField
                        className={styles.commonFormElement}
                        {...form.register('fnrLege', { required: true })}
                        label="Fødselsnummer til lege"
                        defaultValue="20086600138"
                        error={form.formState.errors.fnrLege && 'Fødselsnummer til lege mangler'}
                    />
                    <TextField className={styles.commonFormElement} {...form.register('herId')} label="HER-id" />
                    <TextField
                        className={styles.commonFormElement}
                        {...form.register('hprNummer')}
                        label="HPR-nummer"
                        defaultValue="9144889"
                    />
                    <div className={styles.commonFormElement}>
                        <SyketilfelleStartdato />
                    </div>
                    <div className={styles.commonFormElement}>
                        <Label>Hoveddiagnose</Label>
                        <DiagnosePicker name="hoveddiagnose" diagnoseType="hoveddiagnose" />
                        <BodyShort size="small">
                            Velg tullekode i Diagnosekode for å få en kode som vil bli avslått i systemet!
                        </BodyShort>
                        <Checkbox {...form.register('yrkesskade')}>Yrkesskade</Checkbox>
                    </div>
                    <div className={styles.commonFormElement}>
                        <Label>Bidiagnose</Label>
                        {bidiagnoserFields.map((field, index) => (
                            <DiagnosePicker
                                key={field.id}
                                name={`bidiagnoser.${index}`}
                                diagnoseType="bidiagnose"
                                onRemove={() => bidiagnoserRemove(index)}
                            />
                        ))}
                        <div>
                            <Button
                                variant="secondary"
                                onClick={() =>
                                    bidiagnoserAppend({ system: 'icd10', code: '', text: '' }, { shouldFocus: true })
                                }
                                type="button"
                            >
                                Legg til bidiagnose
                            </Button>
                        </div>
                    </div>
                    <TextField
                        className={styles.commonFormElement}
                        {...form.register('arbeidsgiverNavn')}
                        label="Arbeidsgiver navn"
                    />
                    <TextField
                        className={styles.commonFormElement}
                        {...form.register('meldingTilArbeidsgiver')}
                        label="Melding til arbeidsgiver"
                    />
                    <TextField
                        className={styles.commonFormElement}
                        {...form.register('beskrivBistandNav')}
                        label="Utdype/begrunne hvorfor NAV skal ta opp saken"
                    />
                    <Select
                        {...form.register('annenFraverGrunn')}
                        label="Annen fraværsårsak"
                        className={styles.commonFormElement}
                    >
                        <option value="">Velg</option>
                        <option value="GODKJENT_HELSEINSTITUSJON">
                            Når vedkommende er innlagt i en godkjent helseinstitusjon
                        </option>
                        <option value="BEHANDLING_FORHINDRER_ARBEID">
                            Når vedkommende er under behandling og lege erklærer at behandlingen gjør det nødvendig at
                            vedkommende ikke arbeider
                        </option>
                        <option value="ARBEIDSRETTET_TILTAK">Når vedkommende deltar på et arbeidsrettet tiltak</option>
                        <option value="MOTTAR_TILSKUDD_GRUNNET_HELSETILSTAND">
                            Når vedkommende på grunn av sykdom, skade eller lyte får tilskott når vedkommende på grunn
                            av sykdom, skade eller lyte får tilskott
                        </option>
                        <option value="NODVENDIG_KONTROLLUNDENRSOKELSE">
                            Når vedkommende er til nødvendig kontrollundersøkelse som krever minst 24 timers fravær,
                            reisetid medregnet
                        </option>
                        <option value="SMITTEFARE">
                            Når vedkommende myndighet har nedlagt forbud mot at han eller hun arbeider på grunn av
                            smittefare
                        </option>
                        <option value="ABORT">Når vedkommende er arbeidsufør som følge av svangerskapsavbrudd</option>
                        <option value="UFOR_GRUNNET_BARNLOSHET">
                            Når vedkommende er arbeidsufør som følge av behandling for barnløshet
                        </option>
                        <option value="DONOR">Når vedkommende er donor eller er under vurdering som donor</option>
                        <option value="BEHANDLING_STERILISERING">
                            Når vedkommende er arbeidsufør som følge av behandling i forbindelse med sterilisering
                        </option>
                    </Select>
                    <div className={styles.commonFormElement}>
                        <Behandletdato />
                    </div>
                    <div className={styles.commonFormElement}>
                        <Kontaktdato />
                    </div>
                    <TextField
                        className={styles.commonFormElement}
                        {...form.register('begrunnIkkeKontakt')}
                        label="Tilbakedatering: Begrunnelse"
                    />
                    <Checkbox {...form.register('vedlegg')}>Vedlegg</Checkbox>
                    <Checkbox {...form.register('vedleggMedVirus')}>Vedlegg med virus</Checkbox>
                    <Checkbox {...form.register('virksomhetsykmelding')}>Virksomhetsykmelding</Checkbox>
                    <Select
                        {...form.register('utdypendeOpplysninger')}
                        label="Utdypende opplysninger"
                        className={styles.commonFormElement}
                    >
                        <option value="">Velg</option>
                        <option value="INGEN">Ingen utdypende opplysninger (alle regelsettversjoner)</option>
                        <option value="UKE_7">Utdypende opplysninger ved uke 7</option>
                        <option value="UKE_17"> Utdypende opplysninger ved uke 17</option>
                        <option value="UKE_39">Utdypende opplysninger ved uke 39</option>
                    </Select>
                    <BodyShort size="small">
                        Utdypende opplysninger for uke 7, 17 og 39 påvirker kun regelsettversjon 3.
                    </BodyShort>
                    <TextField
                        className={styles.commonFormElement}
                        {...form.register('regelsettVersjon')}
                        label="Regelsettversjon"
                        defaultValue="3"
                    />
                    {opprettSykmeldingMutation.data && (
                        <FormPage.FormResult variant="success">
                            <BodyShort spacing>Oppretting av sykmelding gikk bra</BodyShort>
                            <BodyShort>{opprettSykmeldingMutation.data.message}</BodyShort>
                        </FormPage.FormResult>
                    )}
                    {sjekkRegelMutation.data != null && (
                        <FormPage.FormResult variant="info">
                            <BodyShort spacing>Regelsjekk OK!</BodyShort>
                            <pre>{JSON.stringify(sjekkRegelMutation.data, null, 2)}</pre>
                        </FormPage.FormResult>
                    )}
                    <FormPage.FormActions>
                        <Button
                            type="submit"
                            loading={opprettSykmeldingMutation.isPending}
                            disabled={sjekkRegelMutation.isPending}
                        >
                            Opprett
                        </Button>
                        <Button
                            variant="secondary"
                            type="button"
                            loading={sjekkRegelMutation.isPending}
                            disabled={opprettSykmeldingMutation.isPending}
                            onClick={async () => {
                                const validationResult = await form.trigger(undefined, {
                                    shouldFocus: true,
                                })
                                if (!validationResult) {
                                    return
                                }
                                opprettSykmeldingMutation.reset()
                                return sjekkRegelMutation.mutate(mapFormValues(form.getValues()))
                            }}
                        >
                            Valider mot regler
                        </Button>
                    </FormPage.FormActions>
                </FormPage>
            </form>
        </FormProvider>
    )
}

function mapFormValues(values: SykmeldingFormValues): SykmeldingFormValues {
    return {
        ...values,
        kontaktDato: values.kontaktDato ? values.kontaktDato : null,
        annenFraverGrunn: values.annenFraverGrunn ? values.annenFraverGrunn : null,
        herId: values.herId ? values.herId : null,
        meldingTilArbeidsgiver: values.meldingTilArbeidsgiver ? values.meldingTilArbeidsgiver : null,
        beskrivBistandNav: values.beskrivBistandNav ? values.beskrivBistandNav : null,
        begrunnIkkeKontakt: values.begrunnIkkeKontakt ? values.begrunnIkkeKontakt : null,
        utdypendeOpplysninger: values.utdypendeOpplysninger ? values.utdypendeOpplysninger : null,
        yrkesskade: values.yrkesskade,
    }
}

export default OpprettSykmeldingForm
