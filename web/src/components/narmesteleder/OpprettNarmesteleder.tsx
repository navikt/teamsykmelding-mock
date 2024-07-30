'use client'

import { ReactElement } from 'react'
import { Button, Checkbox, TextField } from '@navikt/ds-react'
import { FormProvider, useForm } from 'react-hook-form'
import { format } from 'date-fns'

import { useProxyAction } from '../../proxy/api-hooks'
import ProxyFeedback from '../../proxy/proxy-feedback'
import FnrTextField from '../formComponents/FnrTextField'

import AktivFraOgMed from './AktivFraOgMed'

export interface NarmestelederFormValues {
    ansattFnr: string
    lederFnr: string
    orgnummer: string
    mobil: string
    epost: string
    forskutterer: boolean
    aktivFom: string
}

function OpprettNarmesteleder(): ReactElement {
    const dagensDato = format(new Date(), 'yyyy-MM-dd')
    const form = useForm<NarmestelederFormValues>({
        defaultValues: {
            aktivFom: dagensDato,
        },
    })

    const [postData, { error, result, loading }] = useProxyAction<NarmestelederFormValues>('/narmesteleder/opprett')

    return (
        <FormProvider {...form}>
            <form onSubmit={form.handleSubmit((values) => postData(values))} className="flex flex-col gap-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <FnrTextField
                        {...form.register('ansattFnr', { required: true })}
                        label="Fødselsnummer"
                        error={form.formState.errors.ansattFnr && 'Fødselsnummer for den sykmeldte mangler'}
                    />
                    <TextField
                        {...form.register('lederFnr', { required: true })}
                        label="Fødselsnummer til ny nærmeste leder"
                        error={form.formState.errors.lederFnr && 'Fødselsnummer for nærmeste leder mangler'}
                    />
                    <TextField
                        {...form.register('orgnummer', { required: true })}
                        label="Organisasjonsnummer"
                        error={form.formState.errors.orgnummer && 'Organisasjonsnummer mangler'}
                    />
                    <TextField
                        {...form.register('mobil', { required: true })}
                        label="Telefonnummer til ny nærmeste leder"
                        error={form.formState.errors.mobil && 'Telefonnummer for nærmeste leder mangler'}
                    />
                    <TextField
                        {...form.register('epost', { required: true })}
                        label="E-post til ny nærmeste leder"
                        error={form.formState.errors.epost && 'E-post for nærmeste leder mangler'}
                    />
                </div>
                <Checkbox {...form.register('forskutterer')}>Arbeidsgiver forskutterer</Checkbox>
                <AktivFraOgMed />
                <ProxyFeedback error={error} result={result}>
                    <Button type="submit" loading={loading}>
                        Registrer
                    </Button>
                </ProxyFeedback>
            </form>
        </FormProvider>
    )
}

export default OpprettNarmesteleder
