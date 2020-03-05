package com.constellio.data.events.activeMQ;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

public class ActiveMQConnection {

	private final ActiveMQConnectionFactory activeMQConnectionFactory;

	private Connection connection;

	private Session session;

	public ActiveMQConnection(String brokerURL) {
		activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerURL);

	}

	public void createConnection() throws JMSException {
		connection = activeMQConnectionFactory.createConnection();
		connection.start();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	public Session getSession() {
		return this.session;
	}

	public void closeConnection() throws JMSException {
		session.close();
		connection.close();
	}
}
