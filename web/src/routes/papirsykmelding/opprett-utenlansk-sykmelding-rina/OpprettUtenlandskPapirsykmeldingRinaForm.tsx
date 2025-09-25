import { Button, TextField } from '@navikt/ds-react'
import { ReactElement } from 'react'
import { useForm } from 'react-hook-form'

import FnrTextField from '../../../components/form/FnrTextField.tsx'
import { useAction } from '../../../proxy/api-hooks.ts'
import ActionFeedback from '../../../proxy/action-feedback.tsx'
import { CreatedOppgaver, IdType } from '../../../components/CreatedOppgaver.tsx'

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
    const opprettActionResponse = (result as unknown as { message: string; oppgaveId: string } | null) ?? null

    return (
        <form
            onSubmit={handleSubmit((values) =>
                postData(values, {
                    responseMapper: (it) => it,
                }),
            )}
        >
            <FnrTextField {...register('fnr')} label="Fødselsnummer" />
            <TextField
                label="Antall PDFs"
                {...register('antallPdfs', { required: true })}
                error={errors.antallPdfs && 'Antall PDFs mangler'}
            />
            <ActionFeedback error={error} result={opprettActionResponse?.message ?? null}>
                <Button type="submit" loading={loading}>
                    Opprett
                </Button>
            </ActionFeedback>
            {opprettActionResponse?.oppgaveId && (
                <CreatedOppgaver id={opprettActionResponse.oppgaveId} type={IdType.OPPGAVE} norwegian={false} />
            )}
        </form>
    )
}

export default OpprettUtenlandskPapirsykmeldingRinaForm
