import { ReactElement } from 'react'
import { Button, TextField } from '@navikt/ds-react'
import { useForm } from 'react-hook-form'
import { useDelete } from '../../../proxy/api-hooks.ts'
import FnrTextField from '../../../components/form/FnrTextField.tsx'
import ActionFeedback from '../../../proxy/action-feedback.tsx'

interface FormValues {
    fnr: string
    orgnummer: string
}

function SlettNarmestelederForm(): ReactElement {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<FormValues>()

    const [postData, { result, error, loading }] = useDelete('/narmesteleder')

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
            <ActionFeedback error={error} result={result}>
                <Button type="submit" loading={loading}>
                    Deaktiver
                </Button>
            </ActionFeedback>
        </form>
    )
}

export default SlettNarmestelederForm
