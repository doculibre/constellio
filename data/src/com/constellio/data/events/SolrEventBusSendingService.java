package com.constellio.data.events;

import static java.util.Arrays.asList;

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

import javax.xml.bind.DatatypeConverter;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.jetbrains.annotations.NotNull;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;

public class SolrEventBusSendingService extends EventBusSendingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrEventBusSendingService.class);

	String serviceId = UUIDV1Generator.newRandomId();

	Duration pollAndRetrieveFrequency = Duration.standardSeconds(2);

	Duration eventLifespan = Duration.standardMinutes(5);

	SolrClient client;

	Thread sendAndReceiveThread;
	Thread cleanerThread;
	boolean running = true;

	List<Event> sendQueue = new LinkedList<>();

	final int SENDING_BATCH_LIMIT = 50000;
	final int RECEIVING_BATCH_LIMIT = 50000;

	public SolrEventBusSendingService(SolrClient client) {
		this.client = client;

		this.sendAndReceiveThread = new Thread() {
			@Override
			public void run() {
				while (running) {

					List<Event> received = receiveNewEvents();

					receivingEvents(received);

					List<Event> sending = getNewEventsToSend();
					sendNewEvents(sending, received);

					if (sending.size() < SENDING_BATCH_LIMIT && received.size() < RECEIVING_BATCH_LIMIT) {
						try {
							Thread.sleep(pollAndRetrieveFrequency.getMillis());
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}

			@NotNull
			protected List<Event> getNewEventsToSend() {
				List<Event> sending = new ArrayList<>();
				synchronized (sendQueue) {
					Iterator<Event> eventIterator = sendQueue.iterator();
					while (eventIterator.hasNext() && sending.size() < SENDING_BATCH_LIMIT) {
						sending.add(eventIterator.next());
						eventIterator.remove();
					}
				}
				return sending;
			}
		};

		this.cleanerThread = new Thread() {
			@Override
			public void run() {
				super.run();
			}
		};

	}

	protected void receivingEvents(List<Event> received) {
		for (Event event : received) {
			eventReceiver.receive(event);
		}
	}

	private void sendNewEvents(List<Event> sentEvents, List<Event> receivedEvents) {

		List<SolrInputDocument> solrInputDocuments = new ArrayList<>();
		for (Event event : receivedEvents) {
			SolrInputDocument solrInputDocument = new SolrInputDocument();
			solrInputDocument.setField("id", event.getId());

			Map<String, Object> readByAtomicAdd = new HashMap<>();
			readByAtomicAdd.put("add", serviceId);

			solrInputDocument.setField("readBy_ss", readByAtomicAdd);

			solrInputDocuments.add(solrInputDocument);
		}

		for (Event event : sentEvents) {
			SolrInputDocument solrInputDocument = new SolrInputDocument();
			solrInputDocument.setField("id", event.getId());
			solrInputDocument.setField("bus_s", event.getBusName());
			solrInputDocument.setField("timestamp_d", (double) event.getTimeStamp());
			solrInputDocument.setField("type_s", event.getType());
			solrInputDocument.setField("readBy_ss", asList(serviceId));

			Object converted = eventDataSerializer.serialize(event.getData());
			if (converted != null) {
				try {
					solrInputDocument.setField("data_s", serializeToBase64((Serializable) converted));
					solrInputDocuments.add(solrInputDocument);
				} catch (IOException e) {
					LOGGER.warn("Bus event '" + event.getBusName() + "' of type '" + event.getType()
							+ "' cannot be sent since data isn't serialisable", e);
				}
			} else {
				solrInputDocuments.add(solrInputDocument);
			}
		}

		if (!solrInputDocuments.isEmpty()) {
			try {
				client.add(solrInputDocuments);
			} catch (SolrServerException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			try {
				client.commit(true, true, true);
			} catch (SolrServerException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private List<Event> receiveNewEvents() {

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("rows", "" + RECEIVING_BATCH_LIMIT);
		params.set("sort", "timestamp_d asc");
		params.set("q", "-readBy_ss:" + serviceId);

		List<Event> events = new ArrayList<>();
		try {
			for (SolrDocument solrDocument : client.query(params).getResults()) {

				String id = (String) solrDocument.getFieldValue("id");
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
		synchronized (sendQueue) {
			sendQueue.add(event);
		}
	}

	@Override
	public void start() {
		super.start();
		sendAndReceiveThread.start();
	}

	@Override
	public void close() {
		super.close();
		running = false;
		try {
			sendAndReceiveThread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		System.out.println("closed");
	}

	/** Read the object from Base64 string. */
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

	/** Write the object to a Base64 string. */
	private static String serializeToBase64(Serializable o)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return DatatypeConverter.printBase64Binary(baos.toByteArray());
	}

}
