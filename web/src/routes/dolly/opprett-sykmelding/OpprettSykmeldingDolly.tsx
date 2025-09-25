import { ReactElement } from 'react'
import { FormProvider, useFieldArray, useForm } from 'react-hook-form'
import { Button, TextField } from '@navikt/ds-react'

import FnrTextField from '../../../components/form/FnrTextField.tsx'
import { useAction } from '../../../proxy/api-hooks.ts'
import BasicPage from '../../../components/layout/BasicPage.tsx'
import ActionFeedback from '../../../proxy/action-feedback.tsx'

import AktivitetPicker from './AktivitetPicker.tsx'
import { SykmeldingDollyValues } from './Sykmelding.ts'

function OpprettSykmeldingDolly(): ReactElement {
    const form = useForm<SykmeldingDollyValues>({
        defaultValues: {
            ident: '',
            aktivitet: [
                {
                    fom: '',
                    tom: '',
                    grad: undefined,
                },
            ],
        },
    })

    const control = form.control
    const {
        fields: aktivitetFields,
        append,
        remove,
    } = useFieldArray({
        control,
        name: 'aktivitet',
    })

    const [postData, { error, result, loading }] = useAction<SykmeldingDollyValues>('/sykmelding')

    return (
        <BasicPage title="Opprett sykmelding med input-dolly">
            <FormProvider {...form}>
                <form
                    onSubmit={form.handleSubmit((values) => {
                        postData(values)
                    })}
                >
                    <FnrTextField
                        {...form.register('ident', { required: true })}
                        className="my4"
                        label="Fødselsnummer"
                        error={form.formState.errors.ident && 'Fødselsnummer for pasienten mangler'}
                    />
                    {aktivitetFields.map((aktivitet, index) => (
                        <div key={aktivitet.id} className="flex items-end gap-4">
                            <AktivitetPicker name={`aktivitet.${index}`} />
                            <TextField
                                {...form.register(`aktivitet.${index}.grad`, { valueAsNumber: true })}
                                className="w-20"
                                label="Grad"
                                type="tel"
                                inputMode="numeric"
                            />
                            <Button
                                type="button"
                                onClick={() => {
                                    if (aktivitetFields.length > 1) {
                                        remove(index)
                                    }
                                }}
                                variant="tertiary"
                            >
                                Slett
                            </Button>
                        </div>
                    ))}
                    <div className="flex my-5">
                        <Button
                            type="button"
                            onClick={() =>
                                append({
                                    fom: '',
                                    tom: '',
                                    grad: undefined,
                                })
                            }
                        >
                            Legg til periode
                        </Button>
                    </div>
                    <ActionFeedback error={error} result={result}>
                        <Button type="submit" loading={loading}>
                            Opprett
                        </Button>
                    </ActionFeedback>
                </form>
            </FormProvider>
        </BasicPage>
    )
}

export default OpprettSykmeldingDolly
