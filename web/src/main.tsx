import './global.css'

import React from 'react'
import ReactDOM from 'react-dom/client'
import { RouterProvider, createBrowserRouter } from 'react-router-dom'
import Page from './routes/page.tsx'
import NotFound from './routes/not-found.tsx'
import Root from './routes/root.tsx'
import Providers from './providers.tsx'
import OpprettNarmesteleder from './routes/narmesteleder/registrer-narmeste-leder/opprett.tsx'
import SlettNarmesteleder from './routes/narmesteleder/deaktiver-narmeste-leder/slett.tsx'
import OpprettLegeerklaering from './routes/legeerklaering/opprett-legeerklaering/opprett.tsx'
import OpprettSykmeldingForm from './routes/sykmelding/opprett-sykmelding/OpprettSykmeldingForm.tsx'
import SlettSykmelding from './routes/sykmelding/slett-alle-sykmeldinger/slett.tsx'
import OpprettPapirsykmelding from './routes/papirsykmelding/opprett-papirsykmelding/opprett.tsx'
import OpprettUtenlandskPapirSM from './routes/papirsykmelding/opprett-utenlandsk-papirsykmelding/OpprettUtenlandskPapirsykmeldingForm.tsx'
import OpprettUtenlandskPapirSMRina from './routes/papirsykmelding/opprett-utenlansk-sykmelding-rina/opprett.tsx'
import ErrorPage from './routes/error-page.tsx'
import OpprettUtenlandskPapirSMNavNo from './routes/papirsykmelding/opprett-utenlansk-sykmelding-navno/opprett.tsx'

const router = createBrowserRouter([
    {
        path: '/',
        element: <Root />,
        errorElement: <ErrorPage />,
        children: [
            {
                path: '/',
                element: <Page />,
            },
            { path: '/narmesteleder/opprett', element: <OpprettNarmesteleder /> },
            { path: '/narmesteleder/slett', element: <SlettNarmesteleder /> },
            { path: '/legeerklaering/opprett', element: <OpprettLegeerklaering /> },
            { path: '/sykmelding/opprett', element: <OpprettSykmeldingForm /> },
            { path: '/sykmelding/slett', element: <SlettSykmelding /> },
            { path: '/papirsykmelding/opprett', element: <OpprettPapirsykmelding /> },
            {
                path: '/papirsykmelding-utland/opprett',
                element: <OpprettUtenlandskPapirSM />,
            },
            {
                path: '/papirsykmelding-utland-rina/opprett',
                element: <OpprettUtenlandskPapirSMRina />,
            },
            {
                path: '/papirsykmelding-utland-nav-no/opprett',
                element: <OpprettUtenlandskPapirSMNavNo />,
            },
            { path: '*', element: <NotFound /> },
        ],
    },
])

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <Providers>
            <RouterProvider router={router} />
        </Providers>
    </React.StrictMode>,
)
