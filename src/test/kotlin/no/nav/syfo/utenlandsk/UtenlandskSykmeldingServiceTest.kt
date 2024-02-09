package no.nav.syfo.utenlandsk

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.syfo.oppgave.OppgaveClient
import no.nav.syfo.oppgave.OpprettOppgaveResponse
import no.nav.syfo.papirsykmelding.client.DokarkivClient
import no.nav.syfo.utenlandsk.model.UtenlandskSykmeldingNavNoRequest
import no.nav.syfo.utenlandsk.model.UtenlandskSykmeldingPdfRequest
import no.nav.syfo.utenlandsk.service.UtenlandskSykmeldingService
import org.junit.jupiter.api.Test

internal class UtenlandskSykmeldingServiceTest {
    private val dokarkivClient = mockk<DokarkivClient>()
    private val oppgaveClient = mockk<OppgaveClient>()
    private val utenlandskSykmeldingService =
        UtenlandskSykmeldingService(dokarkivClient, oppgaveClient)

    @Test
    internal fun `opprett utenlansk sykmelding Nav med fnr`() {
        coEvery { dokarkivClient.opprettJournalpost(any()) } returns "1"
        coEvery { oppgaveClient.opprettOppgave(any()) } returns
            OpprettOppgaveResponse(
                1,
                1,
            )
        runBlocking {
            utenlandskSykmeldingService.opprettUtenlanskNavNo(
                UtenlandskSykmeldingNavNoRequest(fnr = "123"),
            )
            coVerify { dokarkivClient.opprettJournalpost(match { it.bruker?.id == "123" }) }
        }
    }

    @Test
    internal fun `opprett utenlansk sykmelding Nav uten fnr`() {
        coEvery { dokarkivClient.opprettJournalpost(any()) } returns "1"
        coEvery { oppgaveClient.opprettOppgave(any()) } returns
            OpprettOppgaveResponse(
                1,
                1,
            )
        runBlocking {
            utenlandskSykmeldingService.opprettUtenlanskNavNo(
                UtenlandskSykmeldingNavNoRequest(fnr = null),
            )
            coVerify { dokarkivClient.opprettJournalpost(match { it.bruker?.id == null }) }
        }
    }

    @Test
    internal fun `opprett utenlansk sykmelding pdf uten fnr`() {
        coEvery { dokarkivClient.opprettJournalpost(any()) } returns "1"
        coEvery { oppgaveClient.opprettOppgave(any()) } returns
            OpprettOppgaveResponse(
                1,
                1,
            )
        runBlocking {
            utenlandskSykmeldingService.opprettUtenlanskPdf(
                UtenlandskSykmeldingPdfRequest(fnr = null),
            )
            coVerify { dokarkivClient.opprettJournalpost(match { it.bruker?.id == null }) }
        }
    }
}
