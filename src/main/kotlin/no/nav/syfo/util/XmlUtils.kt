package no.nav.syfo.util

import no.nav.helse.eiFellesformat.XMLEIFellesformat

inline fun <reified T> XMLEIFellesformat.get() = this.any.find { it is T } as T
