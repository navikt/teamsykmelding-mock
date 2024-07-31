import { ReactElement } from 'react'
import {BandageIcon, PersonGroupIcon, PrinterSmallIcon, StethoscopeIcon} from '@navikt/aksel-icons'
import SidebarMenuItem from "./SidebarMenuItem.tsx";
import { Link } from "react-router-dom";

type Props = {
    className?: string;
};

function Sidebar({ className }: Props): ReactElement {
    return (
        <div className={className}>
            <SidebarMenuItem title="Nærmesteleder" Icon={PersonGroupIcon}>
                <li>
                    <Link to="/narmesteleder/opprett">Registrer nærmeste leder</Link>
                </li>
                <li>
                    <Link to="/narmesteleder/slett">Deaktiver nærmeste leder</Link>
                </li>
            </SidebarMenuItem>
            <SidebarMenuItem title="Legeerklæring" Icon={StethoscopeIcon}>
                <li>
                    <Link to="/legeerklaering/opprett">Opprett legeerklæring</Link>
                </li>
            </SidebarMenuItem>
            <SidebarMenuItem title="Sykmelding" Icon={BandageIcon}>
                <li>
                    <Link to="/sykmelding/opprett">Opprett sykmelding</Link>
                </li>
                <li>
                    <Link to="/sykmelding/slett">Slett alle sykmeldinger</Link>
                </li>
            </SidebarMenuItem>
            <SidebarMenuItem title="Papirsykmelding" Icon={PrinterSmallIcon}>
                <li>
                    <Link to="/papirsykmelding/opprett">Opprett papirsykmelding</Link>
                </li>
                <li>
                    <Link to="/papirsykmelding-utland/opprett">Opprett utenlandsk papirsykmelding</Link>
                </li>
                <li>
                    <Link to="/papirsykmelding-utland-rina/opprett">Opprett utenlandsk sykmelding rina</Link>
                </li>
                <li>
                    <Link to="/papirsykmelding-utland-nav-no/opprett">Opprett utenlandsk sykmelding nav.no</Link>
                </li>
            </SidebarMenuItem>
        </div>
    );
}

export default Sidebar
