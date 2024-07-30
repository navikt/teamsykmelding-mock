import { PropsWithChildren, ReactElement } from 'react'
import { Metadata } from 'next'
import { Heading } from '@navikt/ds-react'

type Props = { metadata: Metadata }

function PageContainer({ metadata, children }: PropsWithChildren<Props>): ReactElement {
    return (
        <div>
            <Heading size="large" level="2" spacing>
                {metadata.title as string}
            </Heading>
            {children}
        </div>
    )
}

export default PageContainer
