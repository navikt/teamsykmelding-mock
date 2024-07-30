import { ReactElement } from 'react'
import { Metadata } from 'next'

import OpprettNarmesteleder from '../../../components/narmesteleder/OpprettNarmesteleder'
import PageContainer from '../../../components/layout/page-container'

export const metadata: Metadata = {
    title: 'Registrer nærmeste leder',
}

function Page(): ReactElement {
    return (
        <PageContainer metadata={metadata}>
            <OpprettNarmesteleder />
        </PageContainer>
    )
}

export default Page
