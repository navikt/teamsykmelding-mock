import { PropsWithChildren, ReactElement } from 'react'
import Link from 'next/link'
import { BandageIcon, Buldings2Icon, PrinterSmallIcon, StethoscopeIcon } from '@navikt/aksel-icons'
import { Heading } from '@navikt/ds-react'

function Sidebar(): ReactElement {
    return (
        <div className="p-4 sticky top-0">
            <SidebarHeading Icon={Buldings2Icon}> Nærmesteleder</SidebarHeading>
            <ul className="list-disc pl-4">
                <li>
                    <Link href="/narmesteleder/opprett">Registrer nærmeste leder</Link>
                </li>
                <li>
                    <Link href="/narmesteleder/slett">Deaktiver nærmeste leder</Link>
                </li>
            </ul>

            <SidebarHeading Icon={StethoscopeIcon}>Legeerklæring</SidebarHeading>
            <ul className="list-disc pl-4">
                <li>
                    <Link href="/legeerklaering/opprett">Opprett legeerklæring</Link>
                </li>
            </ul>

            <SidebarHeading Icon={BandageIcon}>Sykmelding</SidebarHeading>
            <ul className="list-disc pl-4">
                <li>
                    <Link href="/sykmelding/opprett">Opprett sykmelding</Link>
                </li>
                <li>
                    <Link href="/sykmelding/slett">Slett alle sykmeldinger</Link>
                </li>
            </ul>

            <SidebarHeading Icon={PrinterSmallIcon}>Papirsykmelding</SidebarHeading>
            <ul className="list-disc pl-4">
                <li>
                    <Link href="/papirsykmelding/opprett">Opprett papirsykmelding</Link>
                </li>
                <li>
                    <Link href="/papirsykmelding-utland/opprett">Opprett utenlandsk papirsykmelding</Link>
                </li>
                <li>
                    <Link href="/papirsykmelding-utland-rina/opprett">Opprett utenlandsk sykmelding rina</Link>
                </li>
                <li>
                    <Link href="/papirsykmelding-utland-nav-no/opprett">Opprett utenlandsk sykmelding nav.no</Link>
                </li>
            </ul>
        </div>
    )
}

function SidebarHeading({ Icon, children }: PropsWithChildren<Readonly<{ Icon: typeof BandageIcon }>>): ReactElement {
    return (
        <div className="flex mt-4 items-center">
            <Icon className="text-2xl m-1" />
            <Heading size="small" level="2">
                {children}
            </Heading>
        </div>
    )
}

export default Sidebar
