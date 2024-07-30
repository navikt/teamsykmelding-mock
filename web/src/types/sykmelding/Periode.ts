/* eslint-disable @typescript-eslint/no-redeclare */
import { z } from 'zod'

export const SykmeldingType = z.enum([
    'AVVENTENDE',
    'GRADERT_UNDER_20',
    'GRADERT_20',
    'GRADERT_40',
    'GRADERT_50',
    'GRADERT_60',
    'GRADERT_80',
    'GRADERT_REISETILSKUDD',
    'HUNDREPROSENT',
    'BEHANDLINGSDAGER',
    'BEHANDLINGSDAG',
    'REISETILSKUDD',
])
export type SykmeldingType = z.infer<typeof SykmeldingType>

export const Periode = z.object({
    fom: z.string(),
    tom: z.string(),
    type: SykmeldingType.nullable(),
})
export type Periode = z.infer<typeof Periode>
