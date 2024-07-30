'use client'

import { ReactElement } from 'react'
import { Button, TextField } from '@navikt/ds-react'
import { useForm } from 'react-hook-form'

import { useProxyDelete } from '../../proxy/api-hooks'
import ProxyFeedback from '../../proxy/proxy-feedback'
import FnrTextField from '../formComponents/FnrTextField'

interface FormValues {
    fnr: string
    orgnummer: string
}

function SlettNarmesteleder(): ReactElement {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<FormValues>()

    const [postData, { result, error, loading }] = useProxyDelete('/narmesteleder')

    return (
        <form
            onSubmit={handleSubmit((formValues) =>
                postData({
                    pathParam: `/${formValues.orgnummer}`,
                    fnr: formValues.fnr,
                }),
            )}
            className="flex gap-4 flex-col"
        >
            <FnrTextField
                {...register('fnr', { required: true })}
                label="Sykmeldtes fødselsnummer"
                error={errors.fnr && 'Fødselsnummer for den sykmeldte mangler'}
            />
            <TextField
                {...register('orgnummer', { required: true })}
                label="Organisasjonsnummer"
                error={errors.orgnummer && 'Organisasjonsnummer mangler'}
            />
            <ProxyFeedback error={error} result={result}>
                <Button type="submit" loading={loading}>
                    Deaktiver
                </Button>
            </ProxyFeedback>
        </form>
    )
}

export default SlettNarmesteleder
