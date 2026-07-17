import { ReactElement } from 'react'
import { BandageIcon, PersonGroupIcon, PrinterSmallIcon, StethoscopeIcon, ExternalLinkIcon } from '@navikt/aksel-icons'
import { Link as AkselLink } from '@navikt/ds-react'
import { Link } from 'react-router-dom'

import SidebarMenuItem from './SidebarMenuItem.tsx'

type Props = {
    className?: string
}

function Sidebar({ className }: Props): ReactElement {
    return (
        <div className={className}>
            <SidebarMenuItem title="Nærmesteleder" Icon={PersonGroupIcon}>
                <SidebarLink to="/narmesteleder/opprett">Registrer nærmeste leder</SidebarLink>
                <SidebarLink to="/narmesteleder/slett">Deaktiver nærmeste leder</SidebarLink>
            </SidebarMenuItem>
            <SidebarMenuItem title="Legeerklæring" Icon={StethoscopeIcon}>
                <SidebarLink to="/legeerklaering/opprett">Opprett legeerklæring</SidebarLink>
            </SidebarMenuItem>
            <SidebarMenuItem title="Sykmelding" Icon={BandageIcon}>
                <SidebarLink to="/sykmelding/opprett">Opprett sykmelding (XML)</SidebarLink>
                <li>
                    <AkselLink
                        href="https://www.ekstern.dev.nav.no/samarbeidspartner/sykmelding"
                        target="_blank"
                        className="-ml-6 flex items-center"
                    >
                        <ExternalLinkIcon aria-hidden />
                        Opprett sykmelding (Ny)
                    </AkselLink>
                </li>
                <SidebarLink to="/sykmelding/slett">Slett alle sykmeldinger</SidebarLink>
            </SidebarMenuItem>
            <SidebarMenuItem title="Papirsykmelding" Icon={PrinterSmallIcon}>
                <SidebarLink to="/papirsykmelding/opprett">Opprett papirsykmelding</SidebarLink>
                <SidebarLink to="/papirsykmelding-utland/opprett">Opprett utenlandsk papirsykmelding</SidebarLink>
                <SidebarLink to="/papirsykmelding-utland-rina/opprett">Opprett utenlandsk sykmelding rina</SidebarLink>
                <SidebarLink to="/papirsykmelding-utland-nav-no/opprett">
                    Opprett utenlandsk sykmelding nav.no
                </SidebarLink>
            </SidebarMenuItem>
        </div>
    )
}

function SidebarLink({ to, children }: { to: string; children: React.ReactNode }): ReactElement {
    return (
        <li>
            <AkselLink as={Link} to={to}>
                {children}
            </AkselLink>
        </li>
    )
}

export default Sidebar
