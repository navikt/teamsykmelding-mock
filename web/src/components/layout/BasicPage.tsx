import { Heading } from '@navikt/ds-react'
import { PropsWithChildren, ReactElement } from 'react'
import cn from 'clsx'

type Props = {
    title: string
    className?: string
}

function BasicPage({ children, className, title }: PropsWithChildren<Props>): ReactElement {
    return (
        <div className={cn('p-6', className)}>
            <Heading size="medium" level="2" className="flex gap-2 items-center">
                {title}
            </Heading>
            <div className="mt-8">{children}</div>
        </div>
    )
}

export default BasicPage
