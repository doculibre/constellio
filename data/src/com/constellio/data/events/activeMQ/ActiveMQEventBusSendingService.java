package com.constellio.data.events.activeMQ;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.events.Event;
import com.constellio.data.events.EventBusListener;
import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_DataIsNotSerializable;
import com.constellio.data.events.EventBusSendingService;
import com.constellio.data.events.SolrEventBusSendingService;
import org.jetbrains.annotations.NotNull;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ActiveMQEventBusSendingService extends EventBusSendingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrEventBusSendingService.class);

	String serviceId = UUIDV1Generator.newRandomId();

	private ActiveMQConnection connection;

	Duration pollAndRetrieveFrequency = Duration.standardSeconds(2);

	AbstractProducer producer;

	AbstractConsumer consumer;

	List<EventReadyToSend> sendQueue = new LinkedList<>();

	Thread sendThread;
	Thread receiveThread;

	final int SENDING_BATCH_LIMIT = 50000;
	boolean running = true;

	public ActiveMQEventBusSendingService(String brokerUrl) {

		this.connection = new ActiveMQConnection(brokerUrl);
		try {
			this.connection.createConnection();
		} catch (JMSException ex) {
			throw new RuntimeException(ex);
		}
		this.producer = new AbstractProducer(connection) {
			@Override
			public void run() {
				while (running) {
					try {
						if (!paused) {
							List<EventReadyToSend> sending = getNewEventsToSend();
							for (EventReadyToSend message : sending) {
								sendMessage(message.event.getType(), message.serializedData);
							}
							if (sending.isEmpty()) {
								Thread.sleep(pollAndRetrieveFrequency.getMillis());
							}
						} else {
							Thread.sleep(50);
						}

					} catch (IllegalStateException e) {
						if (e.getMessage().equals("Connection pool shut down")) {
							try {
								Thread.sleep(50);
							} catch (InterruptedException e1) {
								throw new RuntimeException(e1);
							}
						} else {
							throw e;
						}

					} catch (InterruptedException e) {
						//OK

					} catch (Throwable t) {
						LOGGER.warn("Exception while send/receiving events", t);
					}
				}
			}
		};


		this.sendThread = new Thread(this.producer, "ActiveMQEventSendingService-sendThread");
		//this.receiveThread = new Thread(this.consumer, "ActiveMQEventSendingService-receiveThread");
	}

	private static class EventReadyToSend {
		Event event;
		String serializedData;
	}

	@NotNull
	protected List<EventReadyToSend> getNewEventsToSend() {
		List<EventReadyToSend> sending = new ArrayList<>();
		synchronized (sendQueue) {
			Iterator<EventReadyToSend> eventIterator = sendQueue.iterator();
			while (eventIterator.hasNext() && sending.size() < SENDING_BATCH_LIMIT) {
				sending.add(eventIterator.next());
				eventIterator.remove();
			}
		}
		return sending;
	}

	protected void receivingEvents(List<Event> received) {
		for (Event event : received) {
			getEventReceiver().receive(event);
		}
	}

	@Override
	public void sendRemotely(Event event) {

		String serializedData = null;
		if (event.getData() != null) {
			try {
				Object converted = getEventDataSerializer().serialize(event.getData());
				serializedData = serializeToBase64((Serializable) converted);
			} catch (Throwable t) {
				throw new EventBusManagerRuntimeException_DataIsNotSerializable(event, t);
			}
		}

		EventReadyToSend eventReadyToSend = new EventReadyToSend();
		eventReadyToSend.event = event;
		eventReadyToSend.serializedData = serializedData;

		synchronized (sendQueue) {

			sendQueue.add(eventReadyToSend);
		}
	}

	public ActiveMQEventBusSendingService setPollAndRetrieveFrequency(Duration pollAndRetrieveFrequency) {
		this.pollAndRetrieveFrequency = pollAndRetrieveFrequency;
		return this;
	}

	@Override
	public void start(boolean paused) {
		super.start(paused);
		sendThread.start();
	}

	@Override
	public void close() {
		super.close();
		running = false;
		try {
			this.producer.close();
			sendThread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Read the object from Base64 string.
	 */
	private static Object deserializeBase64(String s)
			throws IOException,
				   ClassNotFoundException {
		byte[] data = DatatypeConverter.parseBase64Binary(s);
		ObjectInputStream ois = new ObjectInputStream(
				new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	/**
	 * Write the object to a Base64 string.
	 */
	private static String serializeToBase64(Serializable o)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return DatatypeConverter.printBase64Binary(baos.toByteArray());
	}

	@Override
	public AbstractConsumer getConsumer(EventBusListener listener, String topic, String busName) {
		return new AbstractConsumer(connection) {
			@Override
			public void run() {
				while (running) {
					try {
						if (!paused) {
							String message = receiveMessage(topic);

							if (message != null) {
								Object data = deserializeBase64(message);
								//TODO Find a way to pass timestamp and id (verify if time could be date when topic was pushed from ActiveMQ)
								listener.onEventReceived(new Event(busName, topic, "", (new Date()).getTime(), data));
							}
							Thread.sleep(pollAndRetrieveFrequency.getMillis());
						} else {
							Thread.sleep(50);
						}

					} catch (IllegalStateException e) {
						if (e.getMessage().equals("Connection pool shut down")) {
							try {
								Thread.sleep(50);
							} catch (InterruptedException e1) {
								throw new RuntimeException(e1);
							}
						} else {
							throw e;
						}

					} catch (InterruptedException e) {
						//OK

					} catch (Throwable t) {
						LOGGER.warn("Exception while send/receiving events", t);
					}
				}
			}
		};
	}
}
