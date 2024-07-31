import { Button, TextField } from '@navikt/ds-react'
import { ReactElement } from 'react'
import { useForm } from 'react-hook-form'
import FnrTextField from '../../../components/formComponents/FnrTextField.tsx'
import { useAction } from '../../../api/proxy/api-hooks.ts'
import ActionFeedback from '../../../api/proxy/action-feedback.tsx'

interface FormValues {
    fnr: string | null
    antallPdfs: number
}

function OpprettUtenlandskPapirsykmeldingRinaForm(): ReactElement {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<FormValues>({
        defaultValues: {
            antallPdfs: 2,
        },
    })

    const [postData, { result, error, loading }] = useAction<FormValues>('/utenlands/opprett')

    return (
        <form onSubmit={handleSubmit((values) => postData(values))} className="flex flex-col gap-4">
            <FnrTextField {...register('fnr')} label="Fødselsnummer" />
            <TextField
                label="Antall PDFs"
                {...register('antallPdfs', { required: true })}
                error={errors.antallPdfs && 'Antall PDFs mangler'}
            />
            <ActionFeedback error={error} result={result}>
                <Button type="submit" loading={loading}>
                    Opprett
                </Button>
            </ActionFeedback>
        </form>
    )
}

export default OpprettUtenlandskPapirsykmeldingRinaForm
