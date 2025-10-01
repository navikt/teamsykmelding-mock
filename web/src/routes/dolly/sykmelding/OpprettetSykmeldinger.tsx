import { ReactElement } from 'react'
import { BodyShort, Heading, List } from '@navikt/ds-react'

import { SykmeldingDollyResponse, SykmeldingerDollyResponse } from '../opprett-sykmelding/Sykmelding.ts'

interface Props {
    data: SykmeldingerDollyResponse
}

function OpprettetSykmeldinger({ data }: Props): ReactElement {
    return (
        <>
            <Heading size="medium" level="2" className="mb-4">
                Opprettet sykmeldinger
            </Heading>
            {data.sykmeldinger.map((sykmelding: SykmeldingDollyResponse) => (
                <div className="mt-4 border-b border-b-border-subtle" key={sykmelding.sykmeldingId}>
                    <BodyShort>{`sykmeldingId: ${sykmelding.sykmeldingId}`}</BodyShort>
                    {sykmelding.aktivitet.map((aktivitet, index) => (
                        <List key={index}>
                            <BodyShort>{`Periode: ${aktivitet.fom} - ${aktivitet.tom}`}</BodyShort>
                            {aktivitet.grad && <List.Item>{`Grad: ${aktivitet.grad}%`}</List.Item>}
                        </List>
                    ))}
                </div>
            ))}
        </>
    )
}

export default OpprettetSykmeldinger
