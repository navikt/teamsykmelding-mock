import { ReactElement, useState } from 'react'
import { Alert, Button, Heading, TextField } from '@navikt/ds-react'
import { useQuery } from '@tanstack/react-query'

import { fetcher } from '../../../api/fetcher.ts'
import { SykmeldingerDollyResponse } from '../opprett-sykmelding/Sykmelding.ts'

import OpprettetSykmeldinger from './OpprettetSykmeldinger.tsx'

function HentSykmeldingerDolly(): ReactElement {
    const [ident, setIdent] = useState<string | null>(null)
    const { data, error, isFetching, refetch } = useQuery({
        queryKey: ['ident', ident],
        queryFn: async () => {
            if (ident === null) {
                throw new Error('SykmeldingId mangler.')
            } else if (ident.length < 11) {
                throw new Error('Fødselsnummer må vere 11 siffer.')
            }
            return await fetcher<SykmeldingerDollyResponse>('/sykmelding/ident', ident)
        },
        enabled: false,
    })

    return (
        <div className="p-6">
            <Heading level="2" size="medium" className="mb-4">
                Hent alle sykmeldinger
            </Heading>
            <TextField
                name="fødselsnummer"
                label="Fødselsnummer"
                size="medium"
                onChange={(event) => {
                    setIdent(event.target.value)
                }}
            />
            <Button className="mt-5 mb-10" loading={isFetching} onClick={() => refetch()}>
                Hent sykmeldinger
            </Button>
            {error ? (
                <Alert variant="error">{error.message}</Alert>
            ) : data ? (
                <OpprettetSykmeldinger data={data} />
            ) : null}
        </div>
    )
}

export default HentSykmeldingerDolly
