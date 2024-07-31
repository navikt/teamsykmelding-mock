import { ReactElement } from 'react'

import BasicPage from '../../../components/layout/BasicPage.tsx'
import OpprettNarmestelederForm from './OpprettNarmestelederForm.tsx'

function OpprettNarmesteleder(): ReactElement {
    return (
        <BasicPage title="Registrer nærmeste leder">
            <OpprettNarmestelederForm />
        </BasicPage>
    )
}

export default OpprettNarmesteleder
