'use client'

import { Alert } from '@navikt/ds-react'
import { useSearchParams } from 'next/navigation'
import { ReactElement } from 'react'

function NewIngressWarning(): ReactElement | null {
    const params = useSearchParams()
    const cameFromoldIngress = params?.get('was-old') ?? false

    if (!cameFromoldIngress) {
        return null
    }

    return (
        <div className="grow mx-8">
            <div className="flex items-center">
                <Alert variant="warning">Mocken har fått ny ingress!</Alert>
                <div className="flex flex-col ml-2">
                    <div>😲 Du ble videresendt fra teamsykmelding-mock.intern.dev.nav.no</div>
                    <div>
                        ℹ️ Oppdater bokmerker o.l. til <b>teamsykmelding-mock.ansatt.dev.nav.no</b>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default NewIngressWarning
