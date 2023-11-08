package no.nav.syfo.papirsykmelding.client

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class DokarkivClientKtTest {
    @Test
    internal fun `create 1 documents`() {
        val dokumenter = opprettUtenlandskJournalpost("fnr", "pdf", 1)
        dokumenter.dokumenter.size shouldBeEqualTo 1
    }

    @Test
    internal fun `create 2 documents`() {
        val dokumenter = opprettUtenlandskJournalpost("fnr", "pdf", 2)
        dokumenter.dokumenter.size shouldBeEqualTo 2
    }

    @Test
    internal fun `create 3 documents`() {
        val dokumenter = opprettUtenlandskJournalpost("fnr", "pdf", 3)
        dokumenter.dokumenter.size shouldBeEqualTo 3
    }

    @Test
    internal fun `create 10 documents`() {
        val dokumenter = opprettUtenlandskJournalpost("fnr", "pdf", 10)
        dokumenter.dokumenter.size shouldBeEqualTo 10
    }
}
