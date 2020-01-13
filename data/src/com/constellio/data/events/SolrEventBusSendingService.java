package com.constellio.data.events;

import com.constellio.data.dao.services.Stats;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_DataIsNotSerializable;
import com.constellio.data.utils.TimeProvider;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.jetbrains.annotations.NotNull;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class SolrEventBusSendingService extends EventBusSendingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrEventBusSendingService.class);

	String serviceId = UUIDV1Generator.newRandomId();

	Duration pollAndRetrieveFrequency = Duration.standardSeconds(2);

	Duration eventLifespan = Duration.standardMinutes(5);

	SolrClient client;

	Thread sendAndReceiveThread;
	Thread cleanerThread;
	boolean running = true;

	List<EventReadyToSend> sendQueue = new LinkedList<>();

	final int SENDING_BATCH_LIMIT = 50000;
	final int RECEIVING_BATCH_LIMIT = 50000;

	public SolrEventBusSendingService(SolrClient client) {
		this.client = client;

		this.sendAndReceiveThread = new Thread() {
			@Override
			public void run() {
				setName("SolrEventBusSendingService-sendAndReceiveThread");
				while (running) {
					try {
						if (!paused) {
							boolean queueEmpty = sendAndReceive();

							if (queueEmpty) {
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

		this.cleanerThread = new Thread() {
			@Override
			public void run() {
				setName("SolrEventBusSendingService-deleteThread");
				while (running) {
					try {
						if (!paused) {
							deleteOldEvents();
							Thread.sleep(60000);

						} else {
							Thread.sleep(50);
						}

					} catch (InterruptedException e) {
						//OK

					} catch (Throwable t) {
						LOGGER.warn("Exception while deleting old events", t);
					}
				}
			}
		};

	}

	boolean sendAndReceive() {

		List<Event> received = Stats.compilerFor("EventBus-receive").log(this::receiveNewEvents);
		Stats.compilerFor("EventBus-handleReceived").log(() -> receivingEvents(received));

		List<EventReadyToSend> sending = getNewEventsToSend();
		if (sending.size() > 0 || received.size() > 0) {
			Stats.compilerFor("EventBus-send").log(() -> sendNewEvents(sending, received));
		}

		return sending.size() < SENDING_BATCH_LIMIT && received.size() < RECEIVING_BATCH_LIMIT;
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

	void deleteOldEvents() {

		Stats.compilerFor("EventBus-clean").log(() -> {
			String query = "timestamp_d:[* TO " + TimeProvider.getLocalDateTime().minus(eventLifespan).toDate().getTime() + "]";
			try {
				client.deleteByQuery(query);
			} catch (SolrServerException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			query = "-timestamp_d:*";
			try {
				client.deleteByQuery(query);
			} catch (SolrServerException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	protected void receivingEvents(List<Event> received) {
		for (Event event : received) {
			eventReceiver.receive(event);
		}
	}

	private static class EventReadyToSend {
		Event event;
		String serializedData;
	}

	private void sendNewEvents(List<EventReadyToSend> sentEvents, List<Event> receivedEvents) {

		List<SolrInputDocument> solrInputDocuments = new ArrayList<>();
		for (Event event : receivedEvents) {
			SolrInputDocument solrInputDocument = new SolrInputDocument();
			solrInputDocument.setField("id", event.getId());

			Map<String, Object> readByAtomicAdd = new HashMap<>();
			readByAtomicAdd.put("add", serviceId);

			solrInputDocument.setField("readBy_ss", readByAtomicAdd);

			solrInputDocuments.add(solrInputDocument);
		}

		for (EventReadyToSend readyToSendEvent : sentEvents) {
			Event event = readyToSendEvent.event;
			SolrInputDocument solrInputDocument = new SolrInputDocument();
			solrInputDocument.setField("id", event.getId());
			solrInputDocument.setField("bus_s", event.getBusName());
			solrInputDocument.setField("timestamp_d", (double) event.getTimeStamp());
			solrInputDocument.setField("type_s", event.getType());
			solrInputDocument.setField("readBy_ss", asList(serviceId));

			if (readyToSendEvent.serializedData != null) {
				solrInputDocument.setField("data_s", readyToSendEvent.serializedData);
			}
			solrInputDocuments.add(solrInputDocument);
		}

		if (!solrInputDocuments.isEmpty()) {
			add(solrInputDocuments);
			commit();
		}

	}

	void add(List<SolrInputDocument> solrInputDocuments) {
		try {
			client.add(solrInputDocuments);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	void commit() {
		try {
			client.commit(true, false, true);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void markExistingEventsHasReceived() {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("rows", "100000");
		params.set("fl", "id");
		params.set("q", "-readBy_ss:" + serviceId);

		List<SolrDocument> events;

		try {
			while (!(events = client.query(params).getResults()).isEmpty()) {

				List<SolrInputDocument> updatedEvents = new ArrayList<>();

				for (SolrDocument event : events) {

					SolrInputDocument solrInputDocument = new SolrInputDocument();
					solrInputDocument.setField("id", event.getFieldValue("id"));

					Map<String, Object> readByAtomicAdd = new HashMap<>();
					readByAtomicAdd.put("add", serviceId);

					solrInputDocument.setField("readBy_ss", readByAtomicAdd);

					updatedEvents.add(solrInputDocument);
				}

				client.add(updatedEvents);
				commit();
			}
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}

	}

	private List<Event> receiveNewEvents() {

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("rows", "" + RECEIVING_BATCH_LIMIT);
		params.set("sort", "timestamp_d asc");
		params.set("q", "-readBy_ss:" + serviceId);
		params.set("fq", "timestamp_d:*");

		List<Event> events = new ArrayList<>();

		try {
			for (SolrDocument solrDocument : client.query(params).getResults()) {
				String id = (String) solrDocument.getFieldValue("id");

				try {
					String type = (String) solrDocument.getFieldValue("type_s");
					long timeStamp = ((Double) solrDocument.getFieldValue("timestamp_d")).longValue();
					String busName = (String) solrDocument.getFieldValue("bus_s");
					String data = (String) solrDocument.getFieldValue("data_s");

					Object deserializedData = null;
					if (data != null) {
						try {
							deserializedData = eventDataSerializer.deserialize(deserializeBase64(data));
						} catch (ClassNotFoundException e) {
							throw new RuntimeException(e);
						}
					}

					events.add(new Event(busName, type, id, timeStamp, deserializedData));

				} catch (Exception e) {
					client.deleteById(id);
					LOGGER.warn("Event could not be deserialized, it is deleted ", e);
				}
			}
		} catch (java.lang.IllegalStateException e) {
			if (e.getMessage().equals("Connection pool shut down")) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					throw new RuntimeException(e1);
				}
			} else {
				throw e;
			}

		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return events;
	}

	public SolrEventBusSendingService setEventLifespan(Duration eventLifespan) {
		this.eventLifespan = eventLifespan;
		return this;
	}

	public SolrEventBusSendingService setPollAndRetrieveFrequency(Duration pollAndRetrieveFrequency) {
		this.pollAndRetrieveFrequency = pollAndRetrieveFrequency;
		return this;
	}

	@Override
	public void sendRemotely(Event event) {

		String serializedData = null;
		if (event.getData() != null) {
			try {
				Object converted = eventDataSerializer.serialize(event.getData());
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

	@Override
	public void start(boolean paused) {
		super.start(paused);
		markExistingEventsHasReceived();
		sendAndReceiveThread.start();
		cleanerThread.start();
	}

	@Override
	public void close() {
		super.close();
		running = false;
		try {
			sendAndReceiveThread.join();
			cleanerThread.interrupt();
			cleanerThread.join();
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

}
