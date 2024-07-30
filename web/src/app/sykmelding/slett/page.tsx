import { ReactElement } from 'react'
import { Metadata } from 'next'

import SlettSykmelding from '../../../components/sykmelding/SlettSykmelding'
import PageContainer from '../../../components/layout/page-container'

export const metadata: Metadata = {
    title: 'Slett alle sykmeldinger',
}

function SlettSM(): ReactElement {
    return (
        <PageContainer metadata={metadata}>
            <SlettSykmelding />
        </PageContainer>
    )
}

export default SlettSM
