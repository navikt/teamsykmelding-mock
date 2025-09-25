import { useRouteError } from 'react-router-dom'
import { BodyShort, Heading } from '@navikt/ds-react'
import { ReactElement } from 'react'

import image from '../images/gostolen-error.webp'

export default function ErrorPage(): ReactElement {
    const error = useRouteError()

    // eslint-disable-next-line no-console
    console.error(error)

    return (
        <div className="h-screen">
            <div className="flex justify-center items-center h-full">
                <div className="flex flex-col justify-center items-center">
                    <img src={image} height={256} width={256} />
                    <Heading size="large" level="2">
                        Hjeeelp en frontend feil!
                    </Heading>
                    <BodyShort>Du drepte Mocken :(</BodyShort>
                </div>
            </div>
        </div>
    )
}
