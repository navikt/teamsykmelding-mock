package no.nav.syfo.narmesteleder.api

import io.kotest.core.spec.style.FunSpec
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.azuread.AccessTokenClient
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.narmesteleder.kafka.NlResponseProducer
import no.nav.syfo.narmesteleder.kafka.model.Leder
import no.nav.syfo.narmesteleder.kafka.model.NlResponse
import no.nav.syfo.narmesteleder.kafka.model.Sykmeldt
import no.nav.syfo.objectMapper
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.model.GetPersonResponse
import no.nav.syfo.pdl.client.model.HentPersonBolk
import no.nav.syfo.pdl.client.model.Navn
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.pdl.client.model.ResponseData
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.withKtor
import org.amshove.kluent.shouldBeEqualTo

class NarmestelederApiKtTest :
    FunSpec({
        val nlResponseProducer = mockk<NlResponseProducer>(relaxed = true)
        val pdlClient = mockk<PdlClient>()
        val accessTokenClient = mockk<AccessTokenClient>()
        val pdlPersonService = PdlPersonService(pdlClient, accessTokenClient, "scope")
        val narmestelederService = NarmestelederService(nlResponseProducer, pdlPersonService)

        val ansattFnr = "12345678910"
        val lederFnr = "10987654321"
        val orgnummer = "888888888"

        beforeTest {
            clearMocks(nlResponseProducer, pdlClient, accessTokenClient)
            coEvery { accessTokenClient.getAccessToken(any()) } returns "token"
        }

        withKtor({ registrerNarmestelederApi(narmestelederService) }) {
            context("NarmestelederApi") {
                test("Oppretter NL-kobling") {
                    coEvery { pdlClient.getPersoner(any(), any()) } returns
                        GetPersonResponse(
                            ResponseData(
                                listOf(
                                    HentPersonBolk(
                                        ansattFnr,
                                        Person(listOf(Navn("Fornavn", null, "Etternavn"))),
                                        "ok"
                                    ),
                                    HentPersonBolk(
                                        lederFnr,
                                        Person(listOf(Navn("Leder", null, "Ledersen"))),
                                        "ok"
                                    ),
                                ),
                            ),
                            null,
                        )
                    val opprettNarmestelederRequest =
                        OpprettNarmestelederRequest(
                            ansattFnr = ansattFnr,
                            lederFnr = lederFnr,
                            orgnummer = orgnummer,
                            mobil = "98989898",
                            epost = "test@nav.no",
                            forskutterer = true,
                            aktivFom = LocalDate.now(),
                        )
                    with(
                        handleRequest(HttpMethod.Post, "/narmesteleder/opprett") {
                            addHeader("Content-Type", ContentType.Application.Json.toString())
                            setBody(objectMapper.writeValueAsString(opprettNarmestelederRequest))
                        },
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.OK
                        response.content shouldBeEqualTo
                            objectMapper.writeValueAsString(
                                HttpMessage("Nærmeste leder er registrert")
                            )
                    }
                    coVerify {
                        nlResponseProducer.sendNlResponse(
                            match {
                                it ==
                                    NlResponse(
                                        orgnummer = orgnummer,
                                        utbetalesLonn = true,
                                        leder =
                                            Leder(
                                                fnr = lederFnr,
                                                mobil = "98989898",
                                                epost = "test@nav.no",
                                                fornavn = "Leder",
                                                etternavn = "Ledersen",
                                            ),
                                        sykmeldt =
                                            Sykmeldt(
                                                fnr = ansattFnr,
                                                navn = "Fornavn Etternavn",
                                            ),
                                        aktivFom =
                                            OffsetDateTime.of(
                                                LocalDate.now().atStartOfDay(),
                                                ZoneOffset.UTC
                                            ),
                                    )
                            },
                            nlAvbrutt = null,
                        )
                    }
                }
                test("Deaktiverer NL-kobling") {
                    with(
                        handleRequest(HttpMethod.Delete, "/narmesteleder/$orgnummer") {
                            addHeader("Sykmeldt-Fnr", ansattFnr)
                        },
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.OK
                    }
                    coVerify {
                        nlResponseProducer.sendNlResponse(
                            nlResponse = null,
                            match { it.orgnummer == orgnummer && it.sykmeldtFnr == ansattFnr },
                        )
                    }
                }
                test("Deaktivering gir 400 hvis fnr mangler") {
                    with(
                        handleRequest(HttpMethod.Delete, "/narmesteleder/$orgnummer"),
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.BadRequest
                    }
                    coVerify(exactly = 0) { nlResponseProducer.sendNlResponse(any(), any()) }
                }
            }
        }
    })
