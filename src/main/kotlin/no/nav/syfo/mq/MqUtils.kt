package no.nav.syfo.mq

import com.ibm.mq.jakarta.jms.MQConnectionFactory
import com.ibm.msg.client.jakarta.wmq.WMQConstants
import com.ibm.msg.client.jakarta.wmq.compat.base.internal.MQC
import jakarta.jms.MessageProducer
import jakarta.jms.Session

interface MqConfig {
    val mqHostname: String
    val mqPort: Int
    val mqGatewayName: String
    val mqChannelName: String
}

fun connectionFactory(config: MqConfig) =
    MQConnectionFactory().apply {
        hostName = config.mqHostname
        port = config.mqPort
        queueManager = config.mqGatewayName
        transportType = WMQConstants.WMQ_CM_CLIENT
        channel = config.mqChannelName
        ccsid = 1208
        sslSocketFactory = null
        sslCipherSuite = null
        setIntProperty(WMQConstants.JMS_IBM_ENCODING, MQC.MQENC_NATIVE)
        setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, 1208)
    }

fun Session.producerForQueue(queueName: String): MessageProducer =
    createProducer(createQueue(queueName))
