export interface SykmeldingDollyValues {
    ident: string
    aktivitet: [Aktivitet]
}

interface Aktivitet {
    fom: string
    tom: string
}

export interface DollySykmeldingResponse {
    sykmeldingId: String
    ident: String
    aktivitet: [Aktivitet]
}
