import { ReactElement } from 'react'
import { Button, Checkbox, Label, TextField } from '@navikt/ds-react'
import { FormProvider, useForm } from 'react-hook-form'
import DiagnosePicker, { Diagnose } from '../../../components/form/DiagnosePicker/DiagnosePicker.tsx'
import FnrTextField from '../../../components/form/FnrTextField.tsx'
import { useAction } from '../../../proxy/api-hooks.ts'
import ActionFeedback from '../../../proxy/action-feedback.tsx'

interface FormValues {
    fnr: string
    fnrLege: string
    statusPresens: string
    vedlegg: boolean
    vedleggMedVirus: boolean
    hoveddiagnose: Diagnose
}

type OpprettLegeerklaeringApiBody = Omit<FormValues, 'hoveddiagnose'> & {
    diagnosekodesystem: 'icd10' | 'icpc2'
    diagnosekode: string
}

function OpprettLegeerklaeringForm(): ReactElement {
    const form = useForm<FormValues>({
        defaultValues: {
            hoveddiagnose: {
                system: 'icd10',
                code: 'H100',
                text: 'Mukopurulent konjunktivitt',
            },
        },
    })

    const [postData, { loading, result, error }] = useAction<OpprettLegeerklaeringApiBody>('/legeerklaering/opprett')

    return (
        <FormProvider {...form}>
            <form
                onSubmit={form.handleSubmit((data) =>
                    postData({
                        fnr: data.fnr,
                        fnrLege: data.fnrLege,
                        statusPresens: data.statusPresens,
                        vedlegg: data.vedlegg,
                        vedleggMedVirus: data.vedleggMedVirus,
                        diagnosekodesystem: data.hoveddiagnose.system,
                        diagnosekode: data.hoveddiagnose.code,
                    }),
                )}
                className="flex gap-4 flex-col"
            >
                <FnrTextField
                    {...form.register('fnr', { required: true })}
                    label="Fødselsnummer"
                    error={form.formState.errors.fnr && 'Fødselsnummer for pasienten mangler'}
                />
                <TextField
                    {...form.register('fnrLege', { required: true })}
                    label="Fødselsnummer til lege"
                    defaultValue="20086600138"
                    error={form.formState.errors.fnrLege && 'Fødselsnummer til lege mangler'}
                />
                <div>
                    <Label>Hoveddiagnose</Label>
                    <DiagnosePicker name="hoveddiagnose" diagnoseType="hoveddiagnose" />
                </div>
                <TextField {...form.register('statusPresens')} label="Status presens" />
                <div>
                    <Checkbox {...form.register('vedlegg')}>Vedlegg</Checkbox>
                    <Checkbox {...form.register('vedleggMedVirus')}>Vedlegg med virus</Checkbox>
                </div>
                <ActionFeedback error={error} result={result}>
                    <Button type="submit" loading={loading}>
                        Opprett
                    </Button>
                </ActionFeedback>
            </form>
        </FormProvider>
    )
}

export default OpprettLegeerklaeringForm
