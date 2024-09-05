import { ReactElement } from 'react'
import { Button } from '@navikt/ds-react'
import { useForm } from 'react-hook-form'
import FnrTextField from '../../../components/form/FnrTextField.tsx'
import { useAction } from '../../../proxy/api-hooks.ts'
import ActionFeedback from '../../../proxy/action-feedback.tsx'

interface FormValues {
    fnr: string | null
}

function OpprettUtenlandskPapirsykmeldingForm(): ReactElement {
    const {
        register,
        handleSubmit,
        formState: {},
    } = useForm<FormValues>()

    const [postData, { result, error, loading }] = useAction<FormValues>('/papirsykmelding/utenlandsk/opprett')

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

export default OpprettUtenlandskPapirsykmeldingForm
