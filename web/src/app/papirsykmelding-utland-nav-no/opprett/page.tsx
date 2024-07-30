import { ReactElement } from 'react'
import { Metadata } from 'next'

import OpprettUtenlandskPapirsykmeldingNavNo from '../../../components/papirsykmelding-utland-nav-no/OpprettUtenlandskPapirsykmeldingNavNo'
import PageContainer from '../../../components/layout/page-container'

export const metadata: Metadata = {
    title: 'Opprett utenlandsk papirsykmelding Nav.no',
}

export default function OpprettUtenlandskPapirSMNavNo(): ReactElement {
    return (
        <PageContainer metadata={metadata}>
            <OpprettUtenlandskPapirsykmeldingNavNo />
        </PageContainer>
    )
}
