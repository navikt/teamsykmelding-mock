package no.nav.syfo.mq

import jakarta.jms.MessageProducer
import jakarta.jms.Session

class MqProducer(
    private val session: Session,
    private val messageProducer: MessageProducer,
) {
    fun send(melding: String) {
        messageProducer.send(session.createTextMessage(melding))
    }
}
