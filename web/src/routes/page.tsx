import { ReactElement } from 'react'
import { BodyShort } from '@navikt/ds-react'

export default function Page(): ReactElement {
    return (
        <div className="p-4">
            <BodyShort spacing>Velkommen til Team Sykmelding sin mock!</BodyShort>
            <BodyShort>Velg hva du vil gjøre i side menyen!</BodyShort>
        </div>
    )
}

export function PageDolly(): ReactElement {
    return (
        <div className="p-4">
            <BodyShort spacing>Velkommen til testside for input-dolly API!</BodyShort>
            <BodyShort>Velg hva du vil gjøre i side menyen.</BodyShort>
        </div>
    )
}
