'use client'

import { Button } from '@navikt/ds-react'
import { ReactElement } from 'react'
import { useForm } from 'react-hook-form'
import {useProxyAction} from "../../../api/proxy/api-hooks.ts";
import FnrTextField from "../../../components/formComponents/FnrTextField.tsx";
import ProxyFeedback from "../../../api/proxy/proxy-feedback.tsx";


interface FormValues {
    fnr: string | null
}

function OpprettUtenlandskPapirsykmeldingNavNoForm(): ReactElement {
    const {
        register,
        handleSubmit,
        formState: {},
    } = useForm<FormValues>()
    const [postData, { result, error, loading }] = useProxyAction<FormValues>('/utenlands/nav/opprett')

    return (
        <form onSubmit={handleSubmit((values) => postData(values))}>
            <FnrTextField {...register('fnr')} label="Fødselsnummer" />
            <ProxyFeedback error={error} result={result}>
                <Button type="submit" loading={loading}>
                    Opprett
                </Button>
            </ProxyFeedback>
        </form>
    )
}

export default OpprettUtenlandskPapirsykmeldingNavNoForm
