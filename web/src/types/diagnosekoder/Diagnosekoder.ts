/* eslint-disable @typescript-eslint/no-redeclare */
import { z } from 'zod'

export enum DiagnosekodeSystem {
    ICD10 = '2.16.578.1.12.4.1.1.7110',
    ICPC2 = '2.16.578.1.12.4.1.1.7170',
}

export const Diagnosekode = z.object({
    code: z.string(),
    text: z.string(),
})
export type Diagnosekode = z.infer<typeof Diagnosekode>

export const Diagnosekoder = z.object({
    [DiagnosekodeSystem.ICD10]: z.array(Diagnosekode),
    [DiagnosekodeSystem.ICPC2]: z.array(Diagnosekode),
})
export type Diagnosekoder = z.infer<typeof Diagnosekoder>
