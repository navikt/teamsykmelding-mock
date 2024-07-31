import { ReactElement } from 'react'
import BasicPage from '../../../components/layout/BasicPage.tsx'
import OpprettLegeerklaeringForm from './OpprettLegeerklaeringForm.tsx'

function OpprettLegeerklaering(): ReactElement {
    return (
        <BasicPage title="Opprett legeerklæring">
            <OpprettLegeerklaeringForm />
        </BasicPage>
    )
}

export default OpprettLegeerklaering
