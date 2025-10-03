import { ReactElement } from 'react'
import { Heading } from '@navikt/ds-react'
import { ChatIcon } from '@navikt/aksel-icons'
import { Link } from 'react-router-dom'

import godstolen from '../../images/godstolen.png'

function HeaderDolly(): ReactElement {
    return (
        <div className="p-4 border-b border-b-border-subtle flex justify-between">
            <Link to="/dolly" className="flex items-center gap-3">
                <img src={godstolen} height="64" className="h-16" alt="Mann i godstolen" />
                <Heading size="large" level="1" className="text-text-default">
                    Team Sykmelding - Dolly
                </Heading>
            </Link>
            <a
                href="https://nav-it.slack.com/archives/CMA3XV997"
                target="_blank"
                rel="noreferrer"
                className="flex items-center"
            >
                <ChatIcon className="mr-2" />
                <span>#team-sykmelding</span>
            </a>
        </div>
    )
}

export default HeaderDolly
