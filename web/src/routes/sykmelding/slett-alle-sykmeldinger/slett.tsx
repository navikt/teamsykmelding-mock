import { ReactElement } from 'react'

import BasicPage from '../../../components/layout/BasicPage.tsx'

import SlettSykmeldingForm from './SlettSykmeldingForm.tsx'

function SlettSykmelding(): ReactElement {
    return (
        <BasicPage title="Slett alle sykmeldinger">
            <SlettSykmeldingForm />
        </BasicPage>
    )
}

export default SlettSykmelding
