import { ReactElement } from 'react'
import { Button } from '@navikt/ds-react'
import { useForm } from 'react-hook-form'
import { useMutation } from '@tanstack/react-query'

import FnrTextField from '../../../components/form/FnrTextField.tsx'
import { CreatedOppgaver, IdType } from '../../../components/CreatedOppgaver.tsx'
import FormPage from '../../../components/layout/FormPage.tsx'
import { post, SimpleMessage } from '../../../api/fetcher.ts'

interface FormValues {
    fnr: string | null
}

function OpprettUtenlandskPapirsykmeldingForm(): ReactElement {
    const { register, handleSubmit } = useForm<FormValues>()

    const opprettMutation = useMutation({
        mutationFn: post<FormValues, SimpleMessage & { journalpostID: string }>('/papirsykmelding/utenlandsk/opprett'),
    })

    return (
        <form onSubmit={handleSubmit((values) => opprettMutation.mutate(values))}>
            <FormPage title="Opprett utenlandsk papirsykmelding" mutations={[opprettMutation]}>
                <FnrTextField {...register('fnr')} label="Fødselsnummer" />
                <FormPage.FormActions>
                    <Button type="submit" loading={opprettMutation.isPending}>
                        Opprett
                    </Button>
                </FormPage.FormActions>
                {opprettMutation.data && (
                    <FormPage.FormResult variant="success">{opprettMutation.data.message}</FormPage.FormResult>
                )}
                {opprettMutation.data?.journalpostID && (
                    <FormPage.FormResult variant="section">
                        <CreatedOppgaver
                            id={opprettMutation.data?.journalpostID}
                            type={IdType.JOURNALPOST}
                            norwegian={false}
                        />
                    </FormPage.FormResult>
                )}
            </FormPage>
        </form>
    )
}

export default OpprettUtenlandskPapirsykmeldingForm
