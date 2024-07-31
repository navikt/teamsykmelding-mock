import { ReactElement } from 'react'
import BasicPage from '../../../components/layout/BasicPage.tsx'
import OpprettUtenlandskPapirsykmeldingRinaForm from './OpprettUtenlandskPapirsykmeldingRinaForm.tsx'

export default function OpprettUtenlandskPapirSMRina(): ReactElement {
    return (
        <BasicPage title="Opprett utenlandsk papirsykmelding RINA">
            <OpprettUtenlandskPapirsykmeldingRinaForm />
        </BasicPage>
    )
}
