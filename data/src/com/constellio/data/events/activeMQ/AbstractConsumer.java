package com.constellio.data.events.activeMQ;

import com.constellio.data.events.Event;
import com.constellio.data.utils.RetryUtil;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;

public class AbstractConsumer implements EventConsumer {

	private ActiveMQConnection connection;
	private Destination destination;
	private RetryUtil retryUtil = new RetryUtil();

	public AbstractConsumer(String brokerName) {
		this.connection = new ActiveMQConnection(brokerName);
		retryUtil.tryThreeTimes(() -> {
			this.connection.createConnection();
			return true;
		});

	}

	public AbstractConsumer(ActiveMQConnection connection) {
		this.connection = connection;
	}

	protected MessageConsumer consumeEvent(String topic) {
		try {
			this.destination = connection.getSession().createTopic(topic);
			MessageConsumer consumer = connection.getSession().createConsumer(destination);
			return consumer;
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	protected String receiveMessage(String topic) {
		MessageConsumer consumer = consumeEvent(topic);
		try {
			Message message = consumer.receive(1000);
			String text;
			if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				text = textMessage.getText();
			} else {
				return null;
			}
			consumer.close();
			return text;
		} catch (JMSException e) {
			try {
				consumer.close();
			} finally {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void run() {

	}

	@Override
	public Event receiveEvent() {
		return null;
	}
}
