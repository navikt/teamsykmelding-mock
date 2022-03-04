package no.nav.syfo.sykmelding.mq

import javax.jms.MessageProducer
import javax.jms.Session

class SyfosmmottakMqProducer(
    private val session: Session,
    private val messageProducer: MessageProducer
) {
    fun send(sykmelding: String) {
        messageProducer.send(session.createTextMessage(sykmelding))
    }
}
