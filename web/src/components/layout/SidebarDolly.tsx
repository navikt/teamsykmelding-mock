import { ReactElement } from 'react'
import { BandageIcon } from '@navikt/aksel-icons'
import SidebarMenuItem from './SidebarMenuItem.tsx'
import { Link } from 'react-router-dom'

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
            </SidebarMenuItem>
        </div>
    )
}

export default SidebarDolly
