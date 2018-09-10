package com.constellio.data.events;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.fail;

public class SDKEventBusSendingService extends EventBusSendingService {

	List<SDKEventBusSendingService> others = new ArrayList<>();

	List<Event> sentEvents = new ArrayList<>();
	List<Event> receivedEvents = new ArrayList<>();

	@Override
	public void sendRemotely(Event event) {
		sentEvents.add(event);

		String serializedDataInBase64 = null;
		if (event.data != null) {
			Object serializedData = eventDataSerializer.serialize(event.data);

			try {
				serializedDataInBase64 = serializeToBase64((Serializable) serializedData);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		for (SDKEventBusSendingService other : others) {
			Object data = null;
			if (serializedDataInBase64 != null) {
				try {
					Object deserializedFromBase64 = deserializeBase64(serializedDataInBase64);
					data = other.eventDataSerializer.deserialize(deserializedFromBase64);
				} catch (IOException | ClassNotFoundException e) {
					throw new RuntimeException(e);
				}

			}
			Event newEvent = new Event(event.getBusName(), event.getType(), event.getId(), event.getTimeStamp(),
					data);
			other.receive(newEvent);
		}
	}

	private Object testDataSerialization(Object data) {
		Object serializedData = eventDataSerializer.serialize(data);
		Object deserializedFromBase64;
		try {
			String serializedDataInBase64 = serializeToBase64((Serializable) serializedData);
			deserializedFromBase64 = deserializeBase64(serializedDataInBase64);
		} catch (Exception e) {
			throw new RuntimeException("Cannot serialize data of type " + data.getClass(), e);
		}

		return eventDataSerializer.deserialize(deserializedFromBase64);

	}

	public void receive(Event event) {
		//System.out.println("Event received " + event.getType() + " " + event.getData());
		receivedEvents.add(event);

		eventReceiver.receive(event);
	}

	public static void interconnect(SDKEventBusSendingService... services) {
		for (int i = 0; i < services.length; i++) {
			for (int j = 0; j < services.length; j++) {
				if (i != j) {
					services[i].others.add(services[j]);
				}
			}
		}
	}

	public List<Event> newReceivedEventsOnBus(String busName) {
		List<Event> newReceived = new ArrayList<>();

		Iterator<Event> iterator = receivedEvents.iterator();

		while (iterator.hasNext()) {
			Event event = iterator.next();
			if (event.getBusName().equals(busName)) {
				newReceived.add(event);
				iterator.remove();
			}
		}

		return newReceived;
	}

	public List<Event> newSentEventsOnBus(String busName) {
		List<Event> newSent = new ArrayList<>();

		Iterator<Event> iterator = sentEvents.iterator();

		while (iterator.hasNext()) {
			Event event = iterator.next();
			if (event.getBusName().equals(busName)) {
				newSent.add(event);
				iterator.remove();
			}
		}

		return newSent;
	}

	public String serialize(Object data) {

		if (data == null) {
			return null;
		}

		String serializedData = null;
		try {
			Object converted = eventDataSerializer.serialize(data);
			serializedData = serializeToBase64((Serializable) converted);
		} catch (Throwable t) {
			t.printStackTrace();
			fail("Data of type '" + data.getClass().getName() + "' is not serialisable");
		}

		return serializedData;
	}


	public Object deserialize(String serialized) {

		if (serialized == null) {
			return null;
		}

		String serializedData = null;
		try {
			Object object = deserializeBase64(serialized);
			Object converted = eventDataSerializer.deserialize(object);
		} catch (Throwable t) {
			t.printStackTrace();
			fail("Failed to deserialize data");
		}

		return serializedData;
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
