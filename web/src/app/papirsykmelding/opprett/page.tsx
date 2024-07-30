import { ReactElement } from 'react'
import { Metadata } from 'next'

import OpprettPapirsykmelding from '../../../components/papirsykmelding/OpprettPapirsykmelding'
import PageContainer from '../../../components/layout/page-container'

export const metadata: Metadata = {
    title: 'Opprett papirsykmelding',
}

function OpprettPapirSM(): ReactElement {
    return (
        <PageContainer metadata={metadata}>
            <OpprettPapirsykmelding />
        </PageContainer>
    )
}

export default OpprettPapirSM
