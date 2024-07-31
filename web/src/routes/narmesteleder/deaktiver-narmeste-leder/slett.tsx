import { ReactElement } from 'react'
import BasicPage from "../../../components/layout/BasicPage.tsx";
import SlettNarmestelederForm from "./SlettNarmestelederForm.tsx";

function SlettNarmesteleder(): ReactElement {
    return (
        <BasicPage title="Deaktiver nærmeste leder">
            <SlettNarmestelederForm />
        </BasicPage>
    )
}

export default SlettNarmesteleder
