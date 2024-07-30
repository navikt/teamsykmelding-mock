import { ReactElement } from 'react'
import { Metadata } from 'next'

import OpprettLegeerklaering from '../../../components/legeerklaering/OpprettLegeerklaering'
import PageContainer from '../../../components/layout/page-container'

export const metadata: Metadata = {
    title: 'Opprett legeerklæring',
}

function OpprettLE(): ReactElement {
    return (
        <PageContainer metadata={metadata}>
            <OpprettLegeerklaering />
        </PageContainer>
    )
}

export default OpprettLE
