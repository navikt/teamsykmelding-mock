import { ReactElement } from 'react'
import { Metadata } from 'next'
import { Heading, BodyShort } from '@navikt/ds-react'

export const metadata: Metadata = {
    title: 'Team Sykmelding Mock | Fant ikke siden',
}

function NotFound(): ReactElement {
    return (
        <div>
            <Heading size="medium" level="2" spacing>
                Fant ikke denne siden!
            </Heading>
            <BodyShort>Kontakt oss gjerne på slack og fortell oss om hvordan du kom til denne siden.</BodyShort>
        </div>
    )
}

export default NotFound
