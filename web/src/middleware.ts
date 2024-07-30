import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'
import { logger } from '@navikt/next-logger'

export function middleware(request: NextRequest): NextResponse | void {
    const url = new URL(request.url)
    const forwardedHostHeader = request.headers.get('x-forwarded-host')

    // Redirect to ingress
    if (forwardedHostHeader?.includes('intern')) {
        logger.info('Hit old ingress, redirecting to new ingress')
        return NextResponse.redirect(
            new URL(`?was-old=true`, 'https://teamsykmelding-mock.ansatt.dev.nav.no' + url.pathname),
        )
    }
}

// See "Matching Paths" below to learn more
export const config = {
    matcher: ['/((?!api|_next/static|_next/image|favicon.ico).*)'],
}
