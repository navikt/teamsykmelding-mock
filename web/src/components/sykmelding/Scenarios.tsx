import React, { PropsWithChildren, ReactElement } from 'react'
import { Heading } from '@navikt/ds-react'
import { useFormContext } from 'react-hook-form'
import { LinkPanel } from '@navikt/ds-react'
import { formatISO } from 'date-fns'

import { subDays } from '../../utils/dateUtils'

import { SykmeldingFormValues } from './OpprettSykmelding'

function Scenarios(): ReactElement {
    const formContext = useFormContext<SykmeldingFormValues>()

    return (
        <>
            <Heading size="medium" level="3" spacing>
                Raske scenarioer (valgfri)
            </Heading>
            <div className="flex gap-3 flex-wrap">
                <ButtonPanel
                    onClick={() => {
                        formContext.reset(manuellBehandlingScenario())
                    }}
                    description="Går til manuell behandling på grunn av tilbakedatering"
                >
                    Manuell behandling
                </ButtonPanel>
                <ButtonPanel
                    onClick={() => {
                        formContext.reset(ugyldigKodeverkScenario())
                    }}
                    description="Ugyldig kodeverk for hoveddiagnose"
                >
                    Ugyldig sykmelding
                </ButtonPanel>
            </div>
        </>
    )
}

function ButtonPanel({
    onClick,
    children,
    description,
}: PropsWithChildren<{ onClick: () => void; description: string }>): ReactElement {
    return (
        <LinkPanel as="button" onClick={onClick} className="text-left p-2">
            <div className="font-bold">{children}</div>
            <div className="text-xs max-w-[169px]">{description}</div>
        </LinkPanel>
    )
}

const manuellBehandlingScenario: () => Partial<SykmeldingFormValues> = () => {
    const now = new Date()

    return {
        perioder: [
            {
                fom: subDays(now, 31),
                tom: subDays(now, 15),
                type: 'HUNDREPROSENT',
            },
        ],
        behandletDato: formatISO(now, { representation: 'date' }),
        begrunnIkkeKontakt: 'Eksempel på begrunnelse for tilbakedaterirng',
    }
}

const ugyldigKodeverkScenario: () => Partial<SykmeldingFormValues> = () => {
    return {
        hoveddiagnose: {
            system: 'icd10',
            code: 'tullekode',
            text: 'Tullekode',
        },
    }
}

export default Scenarios
