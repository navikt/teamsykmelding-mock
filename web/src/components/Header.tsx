import { ReactElement, Suspense } from 'react'
import { Heading } from '@navikt/ds-react'
import Image from 'next/image'
import Link from 'next/link'
import { ChatIcon } from '@navikt/aksel-icons'

import godstolen from '../images/godstolen.png'

import NewIngressWarning from './NewIngressWarning'

function Header(): ReactElement {
    return (
        <div className="p-4 border-b border-b-border-subtle flex justify-between">
            <Link href="/" className="flex items-center gap-3">
                <Image src={godstolen} height="64" alt="Mann i godstolen" />
                <Heading size="large" level="1" className="text-text-default">
                    Team Sykmelding Mock
                </Heading>
            </Link>
            <Suspense>
                <NewIngressWarning />
            </Suspense>
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

export default Header
