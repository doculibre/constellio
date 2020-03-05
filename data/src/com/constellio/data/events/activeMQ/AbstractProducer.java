package com.constellio.data.events.activeMQ;

import com.constellio.data.utils.RetryUtil;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

public abstract class AbstractProducer implements EventProducer {

	private final ActiveMQConnection connection;
	private RetryUtil retryUtil = new RetryUtil();

	public AbstractProducer(String brokerUrl) {
		this.connection = new ActiveMQConnection(brokerUrl);
		retryUtil.tryThreeTimes(() -> {
			this.connection.createConnection();
			return true;
		});
	}

	public AbstractProducer(ActiveMQConnection connection) {
		this.connection = connection;

	}

	protected MessageProducer produceEvent(String topicName) {
		return produceEvent(topicName, DeliveryMode.NON_PERSISTENT);
	}

	protected MessageProducer produceEvent(String topicName, int deliveryMode) {
		try {
			Destination destination = connection.getSession().createTopic(topicName);
			MessageProducer producer = connection.getSession().createProducer(destination);
			producer.setDeliveryMode(deliveryMode);
			return producer;
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	protected void sendMessage(String message, String topic, boolean persistMessage) {
		try {
			int persist = persistMessage ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
			TextMessage textMessage = this.connection.getSession().createTextMessage(message);
			produceEvent(topic, persist).send(textMessage);
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	protected void sendMessage(String message, String topic) {
		try {
			TextMessage textMessage = this.connection.getSession().createTextMessage(message);
			produceEvent(topic).send(textMessage);
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {

	}

	@Override
	public void close() {
		try {
			this.connection.closeConnection();
		} catch (JMSException jmsEx) {
			throw new RuntimeException(jmsEx);
		}
	}

}
