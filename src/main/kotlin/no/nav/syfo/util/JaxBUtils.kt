package no.nav.syfo.util

import no.nav.helse.eiFellesformat.XMLEIFellesformat
import no.nav.helse.msgHead.XMLMsgHead
import no.nav.helse.sm2013.HelseOpplysningerArbeidsuforhet
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.bind.Marshaller.JAXB_FRAGMENT
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamResult
import kotlin.Any
import kotlin.RuntimeException
import kotlin.String
import kotlin.apply

val fellesformatJaxBContext: JAXBContext = JAXBContext.newInstance(XMLEIFellesformat::class.java, XMLMsgHead::class.java, HelseOpplysningerArbeidsuforhet::class.java)
val fellesformatUnmarshaller: Unmarshaller = fellesformatJaxBContext.createUnmarshaller()

val fellesformatMarshaller: Marshaller = fellesformatJaxBContext.createMarshaller().apply {
    setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
    setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    setProperty(JAXB_FRAGMENT, true)
}

fun marshallFellesformat(element: Any): String {
    return try {
        val writer = StringWriter()
        val marshaller: Marshaller = fellesformatJaxBContext.createMarshaller().apply {
            setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
            setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
            setProperty(JAXB_FRAGMENT, true)
        }
        marshaller.marshal(element, StreamResult(writer))
        writer.toString()
    } catch (e: JAXBException) {
        throw RuntimeException(e)
    }
}
