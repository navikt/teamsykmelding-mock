package no.nav.syfo.mq

import jakarta.jms.MessageProducer
import jakarta.jms.Session

class MqProducer(
    private val session: Session,
    private val messageProducer: MessageProducer,
) {
    fun send(melding: String) {
        val message = session.createTextMessage(melding)
        message.setIntProperty("X_Retry_Count", 1)
        messageProducer.send(message)
    }
}
