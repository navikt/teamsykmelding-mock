'use client'

import { Button, TextField } from '@navikt/ds-react'
import { ReactElement } from 'react'
import { useForm } from 'react-hook-form'

import { useProxyAction } from '../../proxy/api-hooks'
import ProxyFeedback from '../../proxy/proxy-feedback'
import FnrTextField from '../formComponents/FnrTextField'

interface FormValues {
    fnr: string | null
    antallPdfs: number
}

function OpprettUtenlandskPapirsykmelding(): ReactElement {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<FormValues>({
        defaultValues: {
            antallPdfs: 2,
        },
    })

    const [postData, { result, error, loading }] = useProxyAction<FormValues>('/utenlands/opprett')

    return (
        <form onSubmit={handleSubmit((values) => postData(values))} className="flex flex-col gap-4">
            <FnrTextField {...register('fnr')} label="Fødselsnummer" />
            <TextField
                label="Antall PDFs"
                {...register('antallPdfs', { required: true })}
                error={errors.antallPdfs && 'Antall PDFs mangler'}
            />
            <ProxyFeedback error={error} result={result}>
                <Button type="submit" loading={loading}>
                    Opprett
                </Button>
            </ProxyFeedback>
        </form>
    )
}

export default OpprettUtenlandskPapirsykmelding
