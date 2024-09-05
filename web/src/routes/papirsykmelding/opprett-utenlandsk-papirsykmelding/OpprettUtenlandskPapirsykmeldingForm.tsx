import { ReactElement, useState } from 'react'
import { Alert, Button, Loader, List, Heading } from '@navikt/ds-react'
import { useForm } from 'react-hook-form'
import FnrTextField from '../../../components/form/FnrTextField.tsx'
import { useAction } from '../../../proxy/api-hooks.ts'
import ActionFeedback from '../../../proxy/action-feedback.tsx'
import { useQuery } from '@tanstack/react-query'

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
                <PollEtterOppgaveId journalPostId={opprettActionResponse.journalpostID} />
            )}
        </form>
    )
}

function PollEtterOppgaveId({ journalPostId }: { journalPostId: string }) {
    const [shouldRefetch, setShouldRefetch] = useState(true)
    const [fetchCount, setFetchCount] = useState(0)
    const isPolling = shouldRefetch && fetchCount <= 10
    const { data, error } = useQuery({
        queryFn: () =>
            fetch(`/api/oppgave/${journalPostId}`)
                .then((res) => res.json())
                .then((result) => {
                    setFetchCount((existing) => existing + 1)
                    if (result.antallTreffTotalt > 0) {
                        setShouldRefetch(false)
                    }
                    return result
                }),
        queryKey: ['oppgave', journalPostId],
        refetchInterval: 1000,
        enabled: isPolling,
    })

    return (
        <div className="p-4">
            <Heading size="small" level="2">
                Tilhørende oppgaveId til journalpost
            </Heading>
            {isPolling && (
                <div className="flex items-center gap-3 p-2 border rounded-md">
                    Prøver å hente oppgaveId til deg :) <Loader size="xsmall" />
                </div>
            )}
            {data != null && data.antallTreffTotalt > 0 && (
                <div>
                    <Heading size="xsmall" level="3">
                        Fant {data.antallTreffTotalt} oppgaver
                    </Heading>
                    <List>
                        {data.oppgaver.map((it: any) => (
                            <List.Item key={it.id}>{it.id}</List.Item>
                        ))}
                    </List>
                </div>
            )}
            {!isPolling && data.antallTreffTotalt === 0 && (
                <div>
                    <Alert variant="error">Klarte ikke å finne noe oppgaveId til deg :(</Alert>
                    <Button onClick={() => setFetchCount(0)}>Prøv mer pls</Button>
                </div>
            )}
            {error && <Alert variant="error">Det skjedde noe krøll i backenden: ${error.message}</Alert>}
        </div>
    )
}

export default OpprettUtenlandskPapirsykmeldingForm
