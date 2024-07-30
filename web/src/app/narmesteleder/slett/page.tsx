import { ReactElement } from 'react'
import { Metadata } from 'next'

import SlettNarmesteleder from '../../../components/narmesteleder/SlettNarmesteleder'
import PageContainer from '../../../components/layout/page-container'

export const metadata: Metadata = {
    title: 'Deaktiver nærmeste leder',
}

function Page(): ReactElement {
    return (
        <PageContainer metadata={metadata}>
            <SlettNarmesteleder />
        </PageContainer>
    )
}

export default Page
