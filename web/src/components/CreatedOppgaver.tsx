import { ReactElement, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Alert, Button, Heading, List, Loader, Link } from '@navikt/ds-react'

function OppgaveLink({ id, norwegian }: { id: string; norwegian: boolean }): ReactElement {
    // Determine the URL based on the norwegian prop
    const oppgaveUrl = norwegian
        ? `https://syk-dig.intern.dev.nav.no/nasjonal/${id}`
        : `https://syk-dig.intern.dev.nav.no/oppgave/${id}`

    return (
        <div>
            <Heading size="xsmall" level="3">
                OppgaveId: {id}
            </Heading>
            <Link href={oppgaveUrl}>åpne oppgave i syk-dig</Link>
        </div>
    )
}
export enum IdType {
    OPPGAVE = 'OPPGAVE',
    JOURNALPOST = 'JOURNALPOST',
}
export function CreatedOppgaver({
    id,
    type,
    norwegian,
}: {
    id: string
    type: IdType
    norwegian: boolean
}): ReactElement {
    const journalpostID = id
    const [shouldRefetch, setShouldRefetch] = useState(true)
    const [fetchCount, setFetchCount] = useState(0)
    const isPolling = shouldRefetch && fetchCount <= 10

    const { data, error } = useQuery({
        queryFn: () =>
            fetch(`/api/oppgave/${journalpostID}`)
                .then((res) => res.json())
                .then((result) => {
                    setFetchCount((existing) => existing + 1)
                    if (result.antallTreffTotalt > 0) {
                        setShouldRefetch(false)
                    }
                    return result
                }),
        queryKey: ['oppgave', journalpostID],
        refetchInterval: 1000,
        enabled: type === IdType.JOURNALPOST && isPolling,
    })

    return (
        <div className="p-4">
            <Heading size="medium" level="2">
                {type === IdType.JOURNALPOST ? 'Tilhørende oppgaveId til journalpost' : 'Oppgave'}
            </Heading>

            {type === IdType.OPPGAVE && <OppgaveLink id={id} norwegian={norwegian} />}

            {type === IdType.JOURNALPOST && (
                <>
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
                                {data.oppgaver.map((it: { id: string }) => (
                                    <List.Item key={it.id}>
                                        <OppgaveLink id={it.id} norwegian={norwegian} /> {/* Reuse OppgaveLink */}
                                    </List.Item>
                                ))}
                            </List>
                        </div>
                    )}
                    {!isPolling && data?.antallTreffTotalt === 0 && (
                        <div>
                            <Alert variant="error">Klarte ikke å finne noe oppgaveId til deg :(</Alert>
                            <Button onClick={() => setFetchCount(0)}>Prøv mer pls</Button>
                        </div>
                    )}
                </>
            )}

            {error && <Alert variant="error">Det skjedde noe krøll i backenden: {error.message}</Alert>}
        </div>
    )
}
