import { Button } from '@navikt/ds-react'
import { ReactElement } from 'react'
import { useForm } from 'react-hook-form'
import { useAction } from '../../../proxy/api-hooks.ts'
import FnrTextField from '../../../components/form/FnrTextField.tsx'
import ActionFeedback from '../../../proxy/action-feedback.tsx'

interface FormValues {
    fnr: string | null
}

function OpprettUtenlandskPapirsykmeldingNavNoForm(): ReactElement {
    const {
        register,
        handleSubmit,
        formState: {},
    } = useForm<FormValues>()
    const [postData, { result, error, loading }] = useAction<FormValues>('/utenlands/nav/opprett')

    return (
        <form onSubmit={handleSubmit((values) => postData(values))}>
            <FnrTextField {...register('fnr')} label="Fødselsnummer" />
            <ActionFeedback error={error} result={result}>
                <Button type="submit" loading={loading}>
                    Opprett
                </Button>
            </ActionFeedback>
        </form>
    )
}

export default OpprettUtenlandskPapirsykmeldingNavNoForm
