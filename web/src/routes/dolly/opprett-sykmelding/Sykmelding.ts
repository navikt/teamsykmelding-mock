export interface SykmeldingDollyValues {
    ident: string
    aktivitet: Aktivitet[]
}

interface Aktivitet {
    fom: string
    tom: string
    grad?: number
}

export interface SykmeldingDollyResponse {
    sykmeldingId: string
    ident: string
    aktivitet: Aktivitet[]
}
