package no.nav.syfo.model

data class JournalpostOpprettetResponse(
    val status: String,
    val message: String,
    val journalpostID: String,
    val automatic: Boolean = true,
)

data class OppgaveOpprettetResponse(val status: String, val message: String, val oppgaveId: String)
