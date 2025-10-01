import { ReactElement } from 'react'
import { BandageIcon } from '@navikt/aksel-icons'
import { Link } from 'react-router-dom'

import SidebarMenuItem from './SidebarMenuItem.tsx'

type Props = {
    className: string
}

function SidebarDolly({ className }: Props): ReactElement {
    return (
        <div className={className}>
            <SidebarMenuItem title="Sykmelding" Icon={BandageIcon}>
                <li>
                    <Link to="/dolly/opprett-sykmelding">Opprett sykmelding</Link>
                </li>
                <li>
                    <Link to="/dolly/sykmelding">Hent sykmelding</Link>
                </li>
                <li>
                    <Link to="/dolly/sykmeldinger">Hent alle sykmeldinger</Link>
                </li>
                <li>
                    <Link to="/dolly/slett-sykmeldinger">Slett sykmeldinger</Link>
                </li>
            </SidebarMenuItem>
        </div>
    )
}

export default SidebarDolly
