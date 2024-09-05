import { Button } from '@navikt/ds-react'
import { ReactElement } from 'react'
import { useForm } from 'react-hook-form'
import { useAction } from '../../../proxy/api-hooks.ts'
import FnrTextField from '../../../components/form/FnrTextField.tsx'
import ActionFeedback from '../../../proxy/action-feedback.tsx'
import { CreatedOppgaver, IdType } from '../../../components/CreatedOppgaver.tsx'

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

export default OpprettUtenlandskPapirsykmeldingNavNoForm
