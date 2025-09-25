import { Button } from '@navikt/ds-react'
import { ReactElement } from 'react'
import { useForm } from 'react-hook-form'

import FnrTextField from '../../../components/form/FnrTextField.tsx'
import { useDelete } from '../../../proxy/api-hooks.ts'
import ActionFeedback from '../../../proxy/action-feedback.tsx'

interface FormValues {
    fnr: string
}

function SlettSykmeldingForm(): ReactElement {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<FormValues>()

    const [postData, { result, error, loading }] = useDelete('/sykmeldinger')

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
            <ActionFeedback error={error} result={result}>
                <Button type="submit" loading={loading}>
                    Slett
                </Button>
            </ActionFeedback>
        </form>
    )
}

export default SlettSykmeldingForm
