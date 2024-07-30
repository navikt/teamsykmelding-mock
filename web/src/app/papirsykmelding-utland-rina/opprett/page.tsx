import { ReactElement } from 'react'
import { Metadata } from 'next'

import OpprettUtenlandskPapirsykmeldingRina from '../../../components/papirsykmelding-utland-rina/OpprettUtenlandskPapirsykmeldingRina'
import PageContainer from '../../../components/layout/page-container'

export const metadata: Metadata = {
    title: 'Opprett utenlandsk papirsykmelding RINA',
}

export default function OpprettUtenlandskPapirSMRina(): ReactElement {
    return (
        <PageContainer metadata={metadata}>
            <OpprettUtenlandskPapirsykmeldingRina />
        </PageContainer>
    )
}
