import { ReactElement } from 'react'
import { Heading, BodyShort } from '@navikt/ds-react'

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
