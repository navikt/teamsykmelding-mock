import { ReactElement } from 'react'
import { BodyShort, Heading, List } from '@navikt/ds-react'
import { DollySykmeldingResponse } from "./Sykmelding.ts"

function OpprettetSykmelding({ sykmelding }: { sykmelding: DollySykmeldingResponse } ): ReactElement {
    return (
        <div className="p-4">
            <Heading size="medium" level="2">
                Opprettet sykmelding
            </Heading>
            <div>
                <Heading size="xsmall" level="3">
                    SykmeldingId:
                </Heading>
                <BodyShort>{sykmelding.sykmeldingId}</BodyShort>
                <Heading size="xsmall" level="3">
                    Aktivitet
                </Heading>
                <List>
                    {sykmelding.aktivitet.map((it: any) => (
                        <List.Item key={it.id}>
                            {it.fom} - {it.tom}
                        </List.Item>
                    ))}
                </List>
            </div>
        </div>
    )
}

export default OpprettetSykmelding
