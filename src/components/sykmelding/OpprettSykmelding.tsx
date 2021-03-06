import { Alert, Button, Checkbox, Heading, Select, TextField } from '@navikt/ds-react';
import { useEffect, useState } from 'react';
import { Controller, useForm, useFieldArray } from 'react-hook-form';
import { format, sub } from 'date-fns';
import { Datepicker } from '@navikt/ds-datepicker';

import { Diagnosekode, Diagnosekoder, DiagnosekodeSystem } from '../../types/diagnosekoder/Diagnosekoder';
import { getDiagnosekoder } from '../../utils/dataUtils';
import { logger } from '../../utils/logger';
import { Periode, SykmeldingType } from '../../types/sykmelding/Periode';

import styles from './OpprettSykmelding.module.css';

interface FormValues {
    fnr: string;
    fnrLege: string;
    herId: string | null;
    hprNummer: string;
    syketilfelleStartdato: string;
    diagnosekodesystem: 'icd10' | 'icpc2';
    diagnosekode: string;
    annenFraverGrunn: string | null;
    perioder: Periode[];
    behandletDato: string;
    kontaktDato: string | undefined;
    begrunnIkkeKontakt: string | null;
    vedlegg: boolean;
    virksomhetsykmelding: boolean;
    utenUtdypendeOpplysninger: boolean;
    regelsettVersjon: string | null;
}

function OpprettSykmelding(): JSX.Element {
    const date = new Date();
    const iGar = format(sub(date, { days: 1 }), 'yyyy-MM-dd');
    const enUkeSiden = format(sub(date, { days: 7 }), 'yyyy-MM-dd');
    const {
        register,
        control,
        handleSubmit,
        formState: { errors },
        watch,
    } = useForm<FormValues>({
        defaultValues: {
            syketilfelleStartdato: enUkeSiden,
            behandletDato: enUkeSiden,
            perioder: [{ fom: enUkeSiden, tom: iGar, type: SykmeldingType.Enum.HUNDREPROSENT }],
        },
    });
    const {
        fields: periodeFields,
        append,
        remove,
    } = useFieldArray({
        control,
        name: 'perioder',
    });
    const [error, setError] = useState<string | null>(null);
    const [result, setResult] = useState<string | null>(null);
    const OPPRETT_SYKMELDING_URL = `/api/proxy/sykmelding/opprett`;
    const [diagnosekoder, setDiagnosekoder] = useState<Diagnosekoder | undefined>(undefined);
    const diagnosekodesystem = watch('diagnosekodesystem');

    useEffect(() => {
        (async () => {
            try {
                const _diagnosekoder = await getDiagnosekoder();
                setDiagnosekoder(_diagnosekoder);
            } catch (error: unknown) {
                logger.error(error);
            }
        })();
    }, []);

    const icd10Koder: Diagnosekode[] = diagnosekoder?.[DiagnosekodeSystem.ICD10] ?? [];
    const icpc2Koder: Diagnosekode[] = diagnosekoder?.[DiagnosekodeSystem.ICPC2] ?? [];

    const postData = async (data: FormValues): Promise<void> => {
        const mappedData = {
            ...data,
            kontaktDato: data.kontaktDato ? data.kontaktDato : null,
            annenFraverGrunn: data.annenFraverGrunn ? data.annenFraverGrunn : null,
            herId: data.herId ? data.herId : null,
            begrunnIkkeKontakt: data.begrunnIkkeKontakt ? data.begrunnIkkeKontakt : null,
        };
        const response = await fetch(OPPRETT_SYKMELDING_URL, {
            method: 'POST',
            body: JSON.stringify(mappedData),
        });

        if (response.ok) {
            setResult((await response.json()).message);
        } else {
            setError((await response.json()).message);
        }
    };

    return (
        <form onSubmit={handleSubmit(postData)}>
            <Heading size="medium" level="2" spacing>
                Opprett sykmelding
            </Heading>
            <TextField
                className={styles.commonFormElement}
                {...register('fnr', { required: true })}
                label="F??dselsnummer"
                error={errors.fnr && 'F??dselsnummer for pasienten mangler'}
            />
            <div className={styles.periodeFields}>
                {periodeFields.map((it, index) => (
                    <div key={it.id} className={styles.periodeFieldRow}>
                        <div>
                            <p>
                                <b>Fom</b>
                            </p>
                            <Controller
                                control={control}
                                name={`perioder.${index}.fom`}
                                render={({ field }) => <Datepicker onChange={field.onChange} value={field.value} />}
                            />
                        </div>
                        <div>
                            <p>
                                <b>Tom</b>
                            </p>
                            <Controller
                                control={control}
                                name={`perioder.${index}.tom`}
                                render={({ field }) => <Datepicker onChange={field.onChange} value={field.value} />}
                            />
                        </div>
                        <Select {...register(`perioder.${index}.type`)} label="Sykmeldingstype">
                            <option value="HUNDREPROSENT">HUNDREPROSENT</option>
                            <option value="AVVENTENDE">AVVENTENDE</option>
                            <option value="GRADERT_20">GRADERT_20</option>
                            <option value="GRADERT_40">GRADERT_40</option>
                            <option value="GRADERT_50">GRADERT_50</option>
                            <option value="GRADERT_60">GRADERT_60</option>
                            <option value="GRADERT_80">GRADERT_80</option>
                            <option value="GRADERT_REISETILSKUDD">GRADERT_REISETILSKUDD</option>
                            <option value="BEHANDLINGSDAGER">BEHANDLINGSDAGER</option>
                            <option value="BEHANDLINGSDAG">BEHANDLINGSDAG</option>
                            <option value="REISETILSKUDD">REISETILSKUDD</option>
                        </Select>
                        <Button type="button" onClick={() => remove(index)} variant="tertiary">
                            Slett
                        </Button>
                    </div>
                ))}
            </div>
            <div className={styles.periodeButton}>
                <Button
                    type="button"
                    onClick={() => append({ fom: enUkeSiden, tom: iGar, type: SykmeldingType.Enum.HUNDREPROSENT })}
                >
                    Legg til periode
                </Button>
            </div>
            <TextField
                className={styles.commonFormElement}
                {...register('fnrLege', { required: true })}
                label="F??dselsnummer til lege"
                defaultValue={'01117302624'}
                error={errors.fnrLege && 'F??dselsnummer til lege mangler'}
            />
            <TextField className={styles.commonFormElement} {...register('herId')} label="HER-id" />
            <TextField
                className={styles.commonFormElement}
                {...register('hprNummer')}
                label="HPR-nummer"
                defaultValue={'7125186'}
            />
            <p>
                <b>Startdato p?? syketilfelle</b>
            </p>
            <Controller
                control={control}
                name="syketilfelleStartdato"
                render={({ field }) => <Datepicker onChange={(date) => field.onChange(date)} value={field.value} />}
            />
            <Select
                {...register('diagnosekodesystem', { required: true })}
                label="Diagnosekodesystem"
                className={styles.commonFormElement}
            >
                <option value="icd10">ICD10</option>
                <option value="icpc2">ICPC2</option>
            </Select>
            {diagnosekodesystem === 'icd10' && (
                <Select
                    {...register('diagnosekode', { required: true })}
                    label="Diagnosekode"
                    description="Velg tullekode for ?? f?? en kode som vil bli avsl??tt i systemet!"
                    className={styles.commonFormElement}
                >
                    {icd10Koder.map((it) => (
                        <option key={it.code} value={it.code}>
                            {it.code + ' - ' + it.text}
                        </option>
                    ))}
                    <option value="tullekode">Tullekode</option>
                </Select>
            )}
            {diagnosekodesystem === 'icpc2' && (
                <Select
                    {...register('diagnosekode', { required: true })}
                    label="Diagnosekode"
                    description="Skriv tullekode for ?? f?? en kode som vil bli avsl??tt i systemet!"
                    className={styles.commonFormElement}
                >
                    {icpc2Koder.map((it) => (
                        <option key={it.code} value={it.code}>
                            {it.code + ' - ' + it.text}
                        </option>
                    ))}
                    <option value="tullekode">Tullekode</option>
                </Select>
            )}
            <Select {...register('annenFraverGrunn')} label="Annen frav??rs??rsak" className={styles.commonFormElement}>
                <option value="">Velg</option>
                <option value="GODKJENT_HELSEINSTITUSJON">
                    N??r vedkommende er innlagt i en godkjent helseinstitusjon
                </option>
                <option value="BEHANDLING_FORHINDRER_ARBEID">
                    N??r vedkommende er under behandling og lege erkl??rer at behandlingen gj??r det n??dvendig at
                    vedkommende ikke arbeider
                </option>
                <option value="ARBEIDSRETTET_TILTAK">N??r vedkommende deltar p?? et arbeidsrettet tiltak</option>
                <option value="MOTTAR_TILSKUDD_GRUNNET_HELSETILSTAND">
                    N??r vedkommende p?? grunn av sykdom, skade eller lyte f??r tilskott n??r vedkommende p?? grunn av
                    sykdom, skade eller lyte f??r tilskott
                </option>
                <option value="NODVENDIG_KONTROLLUNDENRSOKELSE">
                    N??r vedkommende er til n??dvendig kontrollunders??kelse som krever minst 24 timers frav??r, reisetid
                    medregnet
                </option>
                <option value="SMITTEFARE">
                    N??r vedkommende myndighet har nedlagt forbud mot at han eller hun arbeider p?? grunn av smittefare
                </option>
                <option value="ABORT">N??r vedkommende er arbeidsuf??r som f??lge av svangerskapsavbrudd</option>
                <option value="UFOR_GRUNNET_BARNLOSHET">
                    N??r vedkommende er arbeidsuf??r som f??lge av behandling for barnl??shet
                </option>
                <option value="DONOR">N??r vedkommende er donor eller er under vurdering som donor</option>
                <option value="BEHANDLING_STERILISERING">
                    N??r vedkommende er arbeidsuf??r som f??lge av behandling i forbindelse med sterilisering
                </option>
            </Select>
            <p>
                <b>Behandlingsdato</b>
            </p>
            <Controller
                control={control}
                name="behandletDato"
                render={({ field }) => <Datepicker onChange={(date) => field.onChange(date)} value={field.value} />}
            />
            <p>
                <b>Tilbakedatering: Kontaktdato</b>
            </p>
            <Controller
                control={control}
                name="kontaktDato"
                render={({ field }) => <Datepicker onChange={(date) => field.onChange(date)} value={field.value} />}
            />
            <TextField
                className={styles.commonFormElement}
                {...register('begrunnIkkeKontakt')}
                label="Tilbakedatering: Begrunnelse"
            />
            <Checkbox {...register('vedlegg')}>Vedlegg</Checkbox>
            <Checkbox {...register('virksomhetsykmelding')}>Virksomhetsykmelding</Checkbox>
            <Checkbox {...register('utenUtdypendeOpplysninger')}>Uten utdypende opplysninger</Checkbox>
            <TextField
                className={styles.commonFormElement}
                {...register('regelsettVersjon')}
                label="Regelsettversjon"
                defaultValue={'2'}
            />
            <Button type="submit">Opprett</Button>
            {error && <Alert variant="error">{error}</Alert>}
            {result && <Alert variant="success">{result}</Alert>}
        </form>
    );
}

export default OpprettSykmelding;
