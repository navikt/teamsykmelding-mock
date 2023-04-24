package no.nav.syfo.mq

import javax.jms.MessageProducer
import javax.jms.Session

class MqProducer(
    private val session: Session,
    private val messageProducer: MessageProducer,
) {
    fun send(melding: String) {
        messageProducer.send(session.createTextMessage(melding))
    }
}
