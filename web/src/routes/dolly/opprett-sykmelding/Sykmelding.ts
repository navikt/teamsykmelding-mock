export interface SykmeldingDollyValues {
    ident: string
    aktivitet: [Aktivitet]
}

interface Aktivitet {
    fom: string
    tom: string
    grad?: number
}
