import { Button } from '@navikt/ds-react'
import  { ReactElement } from 'react'
import { useForm } from 'react-hook-form'
import FnrTextField from "../../../components/formComponents/FnrTextField.tsx";
import {useProxyDelete} from "../../../api/proxy/api-hooks.ts";
import ProxyFeedback from "../../../api/proxy/proxy-feedback.tsx";


interface FormValues {
    fnr: string
}

function SlettSykmeldingForm(): ReactElement {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<FormValues>()

    const [postData, { result, error, loading }] = useProxyDelete('/sykmeldinger')

    return (
        <form
            onSubmit={handleSubmit((values) =>
                postData({
                    fnr: values.fnr,
                }),
            )}
        >
            <FnrTextField
                {...register('fnr', { required: true })}
                label="Sykmeldtes fødselsnummer"
                error={errors.fnr && 'Fødselsnummer for den sykmeldte mangler'}
            />
            <ProxyFeedback error={error} result={result}>
                <Button type="submit" loading={loading}>
                    Slett
                </Button>
            </ProxyFeedback>
        </form>
    )
}

export default SlettSykmeldingForm
