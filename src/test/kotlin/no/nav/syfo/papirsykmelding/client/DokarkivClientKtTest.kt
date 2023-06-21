package no.nav.syfo.papirsykmelding.client

import io.kotest.core.spec.style.FunSpec
import org.amshove.kluent.shouldBeEqualTo

class DokarkivClientKtTest :
    FunSpec({
        test("create 1 documents") {
            val dokumenter = opprettUtenlandskJournalpost("fnr", "pdf", 1)
            dokumenter.dokumenter.size shouldBeEqualTo 1
        }
        test("create 2 documents") {
            val dokumenter = opprettUtenlandskJournalpost("fnr", "pdf", 2)
            dokumenter.dokumenter.size shouldBeEqualTo 2
        }
        test("create 3 documents") {
            val dokumenter = opprettUtenlandskJournalpost("fnr", "pdf", 3)
            dokumenter.dokumenter.size shouldBeEqualTo 3
        }
        test("create 10 documents") {
            val dokumenter = opprettUtenlandskJournalpost("fnr", "pdf", 10)
            dokumenter.dokumenter.size shouldBeEqualTo 10
        }
    })
