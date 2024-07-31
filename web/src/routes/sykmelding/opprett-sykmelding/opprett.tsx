import { ReactElement } from 'react'
import BasicPage from '../../../components/layout/BasicPage.tsx'
import OpprettSykmeldingForm from './OpprettSykmeldingForm.tsx'

function OpprettSykmelding(): ReactElement {
    return (
        <BasicPage title="Opprett sykmelding">
            <OpprettSykmeldingForm />
        </BasicPage>
    )
}

export default OpprettSykmelding
