import { ReactElement } from 'react'
import { Metadata } from 'next'

import OpprettSykmelding from '../../../components/sykmelding/OpprettSykmelding'
import PageContainer from '../../../components/layout/page-container'

export const metadata: Metadata = {
    title: 'Opprett sykmelding',
}

function OpprettSM(): ReactElement {
    return (
        <PageContainer metadata={metadata}>
            <OpprettSykmelding />
        </PageContainer>
    )
}

export default OpprettSM
