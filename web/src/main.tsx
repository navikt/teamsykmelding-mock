import "./global.css";

import React from "react";
import ReactDOM from "react-dom/client";
import { RouterProvider, createBrowserRouter } from "react-router-dom";

import OpprettNarmesteleder from "@/components/narmesteleder/OpprettNarmesteleder";
import SlettNarmesteleder from "@/components/narmesteleder/SlettNarmesteleder";
import OpprettLegeerklaering from "@/components/legeerklaering/OpprettLegeerklaering";
import OpprettSykmelding from "@/components/sykmelding/OpprettSykmelding";
import SlettSykmelding from "@/components/sykmelding/SlettSykmelding";
import OpprettPapirsykmelding from "@/components/papirsykmelding/OpprettPapirsykmelding";
import OpprettUtenlandskPapirSM from "@/app/papirsykmelding-utland/opprett/page";
import OpprettUtenlandskPapirSMRina from "@/app/papirsykmelding-utland-rina/opprett/page";
import OpprettUtenlandskPapirSMNavNo from "@/app/papirsykmelding-utland-nav-no/opprett/page";
import NotFound from "@/app/not-found";
import {Root} from "postcss";
import ErrorPage from "@/error-page";
import Page from "@/app/page";

const router = createBrowserRouter([
  {
    path: "/",
    element: <Root />,
    errorElement: <ErrorPage />,
    children: [
      {
        path: "/",
        element: <Page />,
      },
      { path: "/narmesteleder/opprett", element: <OpprettNarmesteleder /> },
      { path: "/narmesteleder/slett", element: <SlettNarmesteleder />},
      { path: "/legeerklaering/opprett", element: <OpprettLegeerklaering /> },
      { path: "/sykmelding/opprett", element: <OpprettSykmelding /> },
      { path: "/sykmelding/slett", element: <SlettSykmelding /> },
      { path: "/papirsykmelding/opprett", element: <OpprettPapirsykmelding /> },
      { path: "/papirsykmelding-utland/opprett", element: <OpprettUtenlandskPapirSM /> },
      { path: "/papirsykmelding-utland-rina/opprett", element: <OpprettUtenlandskPapirSMRina /> },
      { path: "/papirsykmelding-utland-nav-no/opprett", element: <OpprettUtenlandskPapirSMNavNo /> },
      { path: "*", element: <NotFound /> },
    ],
  },
]);

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <Providers>
      <RouterProvider router={router} />
    </Providers>
  </React.StrictMode>,
);
