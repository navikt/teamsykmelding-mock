import  { ReactElement } from 'react'
import { Button } from '@navikt/ds-react'
import { useForm } from 'react-hook-form'
import FnrTextField from "../../../components/formComponents/FnrTextField.tsx";
import {useProxyAction} from "../../../api/proxy/api-hooks.ts";
import ProxyFeedback from "../../../api/proxy/proxy-feedback.tsx";

interface FormValues {
    fnr: string | null
}

function OpprettUtenlandskPapirsykmeldingForm(): ReactElement {
    const {
        register,
        handleSubmit,
        formState: {},
    } = useForm<FormValues>()

    const [postData, { result, error, loading }] = useProxyAction<FormValues>('/papirsykmelding/utenlandsk/opprett')

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

export default OpprettUtenlandskPapirsykmeldingForm
