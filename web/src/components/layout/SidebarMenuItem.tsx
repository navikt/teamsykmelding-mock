import { PropsWithChildren, ReactElement } from 'react'
import { Heading } from '@navikt/ds-react'
import { PersonGroupIcon } from '@navikt/aksel-icons'

type Props = {
    title: string
    Icon: typeof PersonGroupIcon
}

function SidebarMenuItem({ title, Icon, children }: PropsWithChildren<Props>): ReactElement {
    return (
        <div className="pb-10">
            <Heading className="flex items-center pb-3" size="medium">
                <Icon className="mr-2" />
                {title}
            </Heading>
            <ul className="pl-8 flex flex-col gap-1">{children}</ul>
        </div>
    )
}

export default SidebarMenuItem
