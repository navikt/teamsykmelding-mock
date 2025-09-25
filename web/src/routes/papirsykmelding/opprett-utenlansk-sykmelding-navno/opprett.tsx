import { ReactElement } from 'react'

import BasicPage from '../../../components/layout/BasicPage.tsx'

import OpprettUtenlandskPapirsykmeldingNavNoForm from './OpprettUtenlandskPapirsykmeldingNavNoForm.tsx'

function OpprettUtenlandskPapirSMNavNo(): ReactElement {
    return (
        <BasicPage title="Opprett utenlandsk papirsykmelding Nav.no">
            <OpprettUtenlandskPapirsykmeldingNavNoForm />
        </BasicPage>
    )
}

export default OpprettUtenlandskPapirSMNavNo
