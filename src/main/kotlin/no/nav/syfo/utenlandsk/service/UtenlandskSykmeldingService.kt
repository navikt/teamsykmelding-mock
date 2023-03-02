package no.nav.syfo.utenlandsk.opprettJournalpostservice

import no.nav.syfo.log
import no.nav.syfo.oppgave.OppgaveClient
import no.nav.syfo.oppgave.OpprettOppgave
import no.nav.syfo.papirsykmelding.PapirsykmeldingService
import no.nav.syfo.papirsykmelding.client.DokarkivClient
import no.nav.syfo.papirsykmelding.client.opprettUtenlandskJournalpost
import no.nav.syfo.utenlandsk.model.UtenlandskSykmeldingRequest
import java.time.LocalDate

class UtenlandskSykmeldingService(
    private val dokarkivClient: DokarkivClient,
    private val oppgaveClient: OppgaveClient
) {
    val utenlandskPdf = PapirsykmeldingService::class.java.getResource("/papirsykmelding/base64utenlandsk")!!.readText(charset = Charsets.ISO_8859_1)
    val defaultMetadata = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2thbm5pbmdtZXRhZGF0YSB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4c2k6bm9OYW1lc3BhY2VTY2hlbWFMb2NhdGlvbj0ic2thbm5pbmdfbWV0YS54c2QiPgogICA8c3lrZW1lbGRpbmdlcj4KICAgICAgPHBhc2llbnQ+CiAgICAgICAgIDxmbnI+MDQwNTkwMzIxMzA8L2Zucj4KICAgICAgPC9wYXNpZW50PgogICAgICA8bWVkaXNpbnNrVnVyZGVyaW5nPgogICAgICAgICA8aG92ZWREaWFnbm9zZT4KICAgICAgICAgICAgPGRpYWdub3Nla29kZT45MTMuNDwvZGlhZ25vc2Vrb2RlPgogICAgICAgICAgICA8ZGlhZ25vc2U+Rm9yc3R1dmluZyBvZyBmb3JzdHJla2tpbmcgaSBjZXJ2aWthbGtvbHVtbmE8L2RpYWdub3NlPgogICAgICAgICA8L2hvdmVkRGlhZ25vc2U+CiAgICAgIDwvbWVkaXNpbnNrVnVyZGVyaW5nPgogICAgICA8YWt0aXZpdGV0PgogICAgICAgICA8YWt0aXZpdGV0SWtrZU11bGlnPgogICAgICAgICAgICA8cGVyaW9kZUZPTURhdG8+MjAxOS0wMS0xMDwvcGVyaW9kZUZPTURhdG8+CiAgICAgICAgICAgIDxwZXJpb2RlVE9NRGF0bz4yMDE5LTAxLTE0PC9wZXJpb2RlVE9NRGF0bz4KICAgICAgICAgICAgPG1lZGlzaW5za2VBcnNha2VyPgogICAgICAgICAgICAgICA8bWVkQXJzYWtlckhpbmRyZXI+MTwvbWVkQXJzYWtlckhpbmRyZXI+CiAgICAgICAgICAgIDwvbWVkaXNpbnNrZUFyc2FrZXI+CiAgICAgICAgIDwvYWt0aXZpdGV0SWtrZU11bGlnPgogICAgICA8L2FrdGl2aXRldD4KICAgICAgPHRpbGJha2VkYXRlcmluZz4KICAgICAgICAgPHRpbGJha2ViZWdydW5uZWxzZT5Ta2FkZWxlZ2V2YWt0ZW4KT3J0b3BlZGlzayBhdmRlbGluZzwvdGlsYmFrZWJlZ3J1bm5lbHNlPgogICAgICA8L3RpbGJha2VkYXRlcmluZz4KICAgICAgPGtvbnRha3RNZWRQYXNpZW50PgogICAgICAgICA8YmVoYW5kbGV0RGF0bz4yMDE5LTAxLTExPC9iZWhhbmRsZXREYXRvPgogICAgICA8L2tvbnRha3RNZWRQYXNpZW50PgogICAgICA8YmVoYW5kbGVyPgogICAgICAgICA8SFBSPjEwMDIzMjQ1PC9IUFI+CiAgICAgIDwvYmVoYW5kbGVyPgogICA8L3N5a2VtZWxkaW5nZXI+Cjwvc2thbm5pbmdtZXRhZGF0YT4="

    suspend fun opprettUtenlanskPdf(utenlandskSykmeldingRequest: UtenlandskSykmeldingRequest) {
        val journalpostId = dokarkivClient.opprettJournalpost(
            opprettUtenlandskJournalpost(
                fnr = utenlandskSykmeldingRequest.fnr,
                pdf = utenlandskPdf,
                antallPdfs = utenlandskSykmeldingRequest.antallPdfs
            )
        )
        log.info("Opprettet journalpost med journalPostId $journalpostId")
        val opprettOppgave = OpprettOppgave(
            opprettetAvEnhetsnr = "9999",
            behandlesAvApplikasjon = null,
            beskrivelse = "Manuell",
            tema = "SYM",
            behandlingsTema = null,
            oppgavetype = "JFR",
            behandlingstype = "ae0106",
            aktivDato = LocalDate.now(),
            fristFerdigstillelse = LocalDate.now().plusDays(3),
            prioritet = "HOY",
            journalpostId = journalpostId,
            metadata = mapOf("RINA_SAKID" to "1234"),
            personident = utenlandskSykmeldingRequest.fnr
        )

        val opprettOppgaveResponse = oppgaveClient.opprettOppgave(opprettOppgave)
        log.info("Oppgave id: ${opprettOppgaveResponse.id}")
    }
}
