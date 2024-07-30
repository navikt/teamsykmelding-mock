import { ReactElement } from 'react'
import { Metadata } from 'next'

import OpprettUtenlandskPapirsykmelding from '../../../components/papirsykmelding-utland/OpprettUtenlandskPapirsykmelding'
import PageContainer from '../../../components/layout/page-container'

export const metadata: Metadata = {
    title: 'Opprett utenlandsk papirsykmelding',
}

function OpprettUtenlandskPapirSM(): ReactElement {
    return (
        <PageContainer metadata={metadata}>
            <OpprettUtenlandskPapirsykmelding />
        </PageContainer>
    )
}

export default OpprettUtenlandskPapirSM
