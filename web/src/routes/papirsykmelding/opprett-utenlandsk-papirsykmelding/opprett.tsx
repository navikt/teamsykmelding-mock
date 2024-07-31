import { ReactElement } from 'react'
import BasicPage from '../../../components/layout/BasicPage.tsx'
import OpprettUtenlandskPapirsykmeldingForm from './OpprettUtenlandskPapirsykmeldingForm.tsx'

function OpprettUtenlandskPapirSM(): ReactElement {
    return (
        <BasicPage title="Opprett utenlandsk papirsykmelding">
            <OpprettUtenlandskPapirsykmeldingForm />
        </BasicPage>
    )
}

export default OpprettUtenlandskPapirSM
