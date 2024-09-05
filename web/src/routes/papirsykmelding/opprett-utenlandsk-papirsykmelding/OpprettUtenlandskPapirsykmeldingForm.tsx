import { ReactElement } from 'react'
import { Button } from '@navikt/ds-react'
import { useForm } from 'react-hook-form'
import FnrTextField from '../../../components/form/FnrTextField.tsx'
import { useAction } from '../../../proxy/api-hooks.ts'
import ActionFeedback from '../../../proxy/action-feedback.tsx'
import { CreatedOppgaver, IdType } from '../../../components/CreatedOppgaver.tsx'

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
    const opprettActionResponse = (result as unknown as { message: string; journalpostID: string } | null) ?? null

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
            {opprettActionResponse?.journalpostID && (
                <CreatedOppgaver id={opprettActionResponse.journalpostID} type={IdType.JOURNALPOST} norwegian={false} />
            )}
        </form>
    )
}

export default OpprettUtenlandskPapirsykmeldingForm
