import { ReactElement } from 'react'
import BasicPage from "../../../components/layout/BasicPage.tsx";
import OpprettPapirsykmeldingForm from "./OpprettPapirsykmeldingForm.tsx";



function OpprettPapirsykmelding(): ReactElement {
    return (
        <BasicPage title="Opprett papirsykmelding">
            <OpprettPapirsykmeldingForm />
        </BasicPage>
    )
}

export default OpprettPapirsykmelding
