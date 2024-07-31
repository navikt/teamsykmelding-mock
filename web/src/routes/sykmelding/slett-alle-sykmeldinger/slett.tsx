import { ReactElement } from 'react'
import SlettSykmeldingForm from './SlettSykmeldingForm.tsx'
import BasicPage from '../../../components/layout/BasicPage.tsx'

function SlettSykmelding(): ReactElement {
    return (
        <BasicPage title="Slett alle sykmeldinger">
            <SlettSykmeldingForm />
        </BasicPage>
    )
}

export default SlettSykmelding
