import { Button, Checkbox, Select, TextField } from '@navikt/ds-react'
import { ReactElement } from 'react'
import { FormProvider, useFieldArray, useForm } from 'react-hook-form'
import { format, sub } from 'date-fns'

import styles from './OpprettPapirsykmelding.module.css'
import Behandletdato from './Behandletdato'
import SyketilfelleStartdato from './SyketilfelleStartdato'
import { Periode, SykmeldingType } from '../../../types/sykmelding/Periode.ts'
import DiagnosePicker, { Diagnose } from '../../../components/form/DiagnosePicker/DiagnosePicker.tsx'
import PeriodePicker from '../../../components/form/PeriodePicker/PeriodePicker.tsx'
import FnrTextField from '../../../components/form/FnrTextField.tsx'
import ActionFeedback from '../../../proxy/action-feedback.tsx'
import { useAction } from '../../../proxy/api-hooks.ts'
import { CreatedOppgaver, IdType } from '../../../components/CreatedOppgaver.tsx'

export interface PapirsykmeldingFormValues {
    fnr: string | null
    hprNummer: string
    syketilfelleStartdato: string
    behandletDato: string
    perioder: Periode[]
    utenOcr: boolean
    hoveddiagnose: Diagnose
}

type OpprettPapirsykmeldingApiBody = Omit<PapirsykmeldingFormValues, 'hoveddiagnose'> & {
    diagnosekodesystem: 'icd10' | 'icpc2'
    diagnosekode: string
}

function OpprettPapirsykmeldingForm(): ReactElement {
    const date = new Date()
    const iGar = format(sub(date, { days: 1 }), 'yyyy-MM-dd')
    const enUkeSiden = format(sub(date, { days: 7 }), 'yyyy-MM-dd')
    const form = useForm<PapirsykmeldingFormValues>({
        defaultValues: {
            syketilfelleStartdato: enUkeSiden,
            behandletDato: enUkeSiden,
            perioder: [{ fom: enUkeSiden, tom: iGar, type: SykmeldingType.Enum.HUNDREPROSENT }],
            hoveddiagnose: {
                system: 'icd10',
                code: 'H100',
                text: 'Mukopurulent konjunktivitt',
            },
        },
    })
    const control = form.control
    const {
        fields: periodeFields,
        append,
        remove,
    } = useFieldArray({
        control,
        name: 'perioder',
    })

    const [postData, { error, result, loading, reset }] =
        useAction<OpprettPapirsykmeldingApiBody>('/papirsykmelding/opprett')
    const [postDataRegelsjekk, { error: regelError, result: regelResult, loading: regelLoading, reset: regelReset }] =
        useAction<OpprettPapirsykmeldingApiBody>('/papirsykmelding/regelsjekk')
    const opprettActionResponse =
        (result as unknown as { message: string; journalpostID: string; automatic: boolean } | null) ?? null

    return (
        <FormProvider {...form}>
            <form
                onSubmit={form.handleSubmit((values) => {
                    regelReset()
                    postData(
                        {
                            fnr: values.fnr,
                            hprNummer: values.hprNummer,
                            syketilfelleStartdato: values.syketilfelleStartdato,
                            behandletDato: values.behandletDato,
                            perioder: values.perioder,
                            utenOcr: values.utenOcr,
                            diagnosekodesystem: values.hoveddiagnose.system,
                            diagnosekode: values.hoveddiagnose.code,
                        },
                        {
                            responseMapper: (it) => it,
                        },
                    )
                })}
            >
                <FnrTextField className={styles.commonFormElement} {...form.register('fnr')} label="Fødselsnummer" />
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
                            <Button type="button" onClick={() => remove(index)} variant="tertiary">
                                Slett
                            </Button>
                        </div>
                    ))}
                </div>
                <div className={styles.periodeButton}>
                    <Button
                        type="button"
                        onClick={() =>
                            append({
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
                    {...form.register('hprNummer')}
                    label="HPR-nummer"
                    defaultValue="9144889"
                />
                <div className={styles.commonFormElement}>
                    <SyketilfelleStartdato />
                </div>
                <div className={styles.commonFormElement}>
                    <Behandletdato />
                </div>
                <p>
                    <b>Hoveddiagnose</b>
                </p>
                <DiagnosePicker name="hoveddiagnose" diagnoseType="hoveddiagnose" />

                <Checkbox {...form.register('utenOcr')}>Opprett papirsykmelding uten OCR</Checkbox>
                <ActionFeedback
                    error={regelError ?? error}
                    result={regelResult ?? opprettActionResponse?.message ?? result}
                >
                    <Button type="submit" loading={loading} disabled={regelLoading}>
                        Opprett
                    </Button>
                    <Button
                        variant="secondary"
                        type="button"
                        loading={regelLoading}
                        disabled={loading}
                        onClick={async () => {
                            const validationResult = await form.trigger(undefined, {
                                shouldFocus: true,
                            })
                            if (!validationResult) {
                                return
                            }
                            reset()
                            const values = form.getValues()
                            return postDataRegelsjekk(
                                {
                                    fnr: values.fnr ? values.fnr : null,
                                    hprNummer: values.hprNummer,
                                    syketilfelleStartdato: values.syketilfelleStartdato,
                                    behandletDato: values.behandletDato,
                                    perioder: values.perioder,
                                    utenOcr: values.utenOcr,
                                    diagnosekodesystem: values.hoveddiagnose.system,
                                    diagnosekode: values.hoveddiagnose.code,
                                },
                                {
                                    responseMapper: (response) => JSON.stringify(response, null, 2),
                                },
                            )
                        }}
                    >
                        Valider mot regler
                    </Button>
                </ActionFeedback>
                {opprettActionResponse?.automatic === false && (
                    <CreatedOppgaver
                        id={opprettActionResponse.journalpostID}
                        type={IdType.JOURNALPOST}
                        norwegian={true}
                    />
                )}
            </form>
        </FormProvider>
    )
}

export default OpprettPapirsykmeldingForm
