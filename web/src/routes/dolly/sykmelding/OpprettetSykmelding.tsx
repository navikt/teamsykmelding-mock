import { ReactElement } from 'react'
import { BodyShort, Heading, List } from '@navikt/ds-react'
import { SykmeldingDollyResponse } from '../opprett-sykmelding/Sykmelding.ts'

interface Props {
    data: SykmeldingDollyResponse
}

function OpprettetSykmelding({ data }: Props): ReactElement {
    return (
        <div>
            <Heading size="medium" level="2" className="mb-4">Opprettet sykmelding</Heading>
            <BodyShort>{`sykmeldingId: ${data.sykmeldingId}`}</BodyShort>
            {data.aktivitet.map((aktivitet, index) => (
                <List key={index}>
                    <BodyShort>{`Periode: ${aktivitet.fom} - ${aktivitet.tom}`}</BodyShort>
                    {aktivitet.grad && <List.Item>{`Grad: ${aktivitet.grad}%`}</List.Item>}
                </List>
            ))}
        </div>
    )
}

export default OpprettetSykmelding
