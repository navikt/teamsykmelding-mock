import { Button, Heading } from '@navikt/ds-react'
import { ReactElement } from 'react'
import { useForm } from 'react-hook-form'

import FnrTextField from '../../../components/form/FnrTextField.tsx'
import { useDelete } from '../../../proxy/api-hooks.ts'
import ActionFeedback from '../../../proxy/action-feedback.tsx'

interface FormValues {
    fnr: string
}

function SlettSykmeldingerDolly(): ReactElement {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<FormValues>()

    const [postData, { result, error, loading }] = useDelete('/sykmelding/ident')

    return (
        <form
            className="p-6"
            onSubmit={handleSubmit((values) =>
                postData({
                    fnr: values.fnr,
                }),
            )}
        >
            <Heading className="mb-4" level="2" size="medium">
                Slett alle sykmeldinger
            </Heading>
            <FnrTextField
                {...register('fnr', {
                    required: 'Fødselsnummer mangler.',
                    minLength: { value: 11, message: 'Fødselsnummer må vere 11 siffer.' },
                })}
                label="Fødselsnummer"
                error={errors.fnr?.message}
            />
            <ActionFeedback error={error} result={result}>
                <Button type="submit" loading={loading}>
                    Slett
                </Button>
            </ActionFeedback>
        </form>
    )
}

export default SlettSykmeldingerDolly
