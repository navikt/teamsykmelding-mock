package no.nav.syfo.utils

import com.migesok.jaxb.adapter.javatime.LocalDateTimeXmlAdapter
import com.migesok.jaxb.adapter.javatime.LocalDateXmlAdapter
import java.io.StringWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.xml.bind.DatatypeConverter
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
import no.nav.helse.eiFellesformat.XMLEIFellesformat
import no.nav.helse.legeerklaering.Legeerklaring
import no.nav.helse.msgHead.XMLMsgHead
import no.nav.helse.papirsykemelding.Skanningmetadata
import no.nav.helse.sm2013.HelseOpplysningerArbeidsuforhet

val fellesformatJaxBContext: JAXBContext =
    JAXBContext.newInstance(
        XMLEIFellesformat::class.java,
        XMLMsgHead::class.java,
        HelseOpplysningerArbeidsuforhet::class.java
    )
val fellesformatUnmarshaller: Unmarshaller = fellesformatJaxBContext.createUnmarshaller()

val legeerklaeringJaxBContext: JAXBContext =
    JAXBContext.newInstance(
        XMLEIFellesformat::class.java,
        XMLMsgHead::class.java,
        Legeerklaring::class.java
    )
val legeerklaeringUnmarshaller: Unmarshaller = legeerklaeringJaxBContext.createUnmarshaller()

val jaxbContextSkanningmetadata: JAXBContext = JAXBContext.newInstance(Skanningmetadata::class.java)

fun marshallFellesformat(element: Any): String {
    return try {
        val writer = StringWriter()
        val marshaller: Marshaller =
            fellesformatJaxBContext.createMarshaller().apply {
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

fun marshallLegeerklaering(element: Any): String {
    return try {
        val writer = StringWriter()
        val marshaller: Marshaller =
            legeerklaeringJaxBContext.createMarshaller().apply {
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

class XMLDateTimeAdapter : LocalDateTimeXmlAdapter() {
    override fun unmarshal(stringValue: String?): LocalDateTime? =
        when (stringValue) {
            null -> null
            else ->
                DatatypeConverter.parseDateTime(stringValue)
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime()
        }
}

class XMLDateAdapter : LocalDateXmlAdapter() {
    override fun unmarshal(stringValue: String?): LocalDate? =
        when (stringValue) {
            null -> null
            else ->
                DatatypeConverter.parseDate(stringValue)
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
        }
}
