import { ReactElement, useState } from 'react'
import { Alert, Button, Heading, TextField } from '@navikt/ds-react'
import { useQuery } from '@tanstack/react-query'

import { fetcher } from '../../../api/fetcher.ts'
import { SykmeldingDollyResponse } from '../opprett-sykmelding/Sykmelding.ts'

import OpprettetSykmelding from './OpprettetSykmelding.tsx'

function HentSykmeldingDolly(): ReactElement {
    const [sykmeldingId, setSykmeldingId] = useState<string | null>(null)
    const { data, error, isFetching, refetch } = useQuery({
        queryKey: ['sykmelding', sykmeldingId],
        queryFn: async () => {
            if (sykmeldingId === null) {
                throw new Error('SykmeldingId mangler.')
            }
            return await fetcher<SykmeldingDollyResponse>(`/sykmelding/${sykmeldingId}`)
        },
        enabled: false,
    })

    return (
        <div className="p-6">
            <Heading level="2" size="medium" className="mb-4">
                Hent sykmelding
            </Heading>
            <TextField
                name="sykmeldingId"
                label="SykmeldingId"
                size="medium"
                onChange={(event) => {
                    setSykmeldingId(event.target.value)
                }}
            />
            <Button className="mt-5 mb-10" loading={isFetching} onClick={() => refetch()}>
                Hent sykmelding
            </Button>
            {error ? <Alert variant="error">{error.message}</Alert> : data ? <OpprettetSykmelding data={data} /> : null}
        </div>
    )
}

export default HentSykmeldingDolly
