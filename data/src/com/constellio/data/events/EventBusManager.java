package com.constellio.data.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_EventBusAlreadyExist;
import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_NoSuchEventBus;

public abstract class EventBusManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventBusManager.class);

	protected Map<String, EventBus> eventBuses = new HashMap<>();
	protected Map<String, Semaphore> awaitExecution = new HashMap<>();

	protected List<EventDataSerializer> eventDataSerializers = new ArrayList<>();

	public EventBus getEventBus(String name) {
		EventBus eventBus = eventBuses.get(name);
		if (eventBus == null) {
			throw new EventBusManagerRuntimeException_NoSuchEventBus(name);
		}
		return eventBus;
	}

	public boolean hasEventBus(String name) {
		return eventBuses.containsKey(name);
	}

	public boolean removeEventBus(String name) {
		return eventBuses.remove(name) != null;
	}

	public EventBus createEventBus(String name) {
		EventBus eventBus = eventBuses.get(name);
		if (eventBus != null) {
			throw new EventBusManagerRuntimeException_EventBusAlreadyExist(name);
		}
		eventBus = new EventBus(name, this);
		eventBuses.put(name, eventBus);
		return eventBus;
	}

	public Semaphore sendAndAwaitExecution(Event event) {
		//		LOGGER.info("Sending event " + event.id + " of type " + event.getType() + " on bus " + event.busName + " with data "
		//				+ event.data);
		Semaphore semaphore = new Semaphore(1);
		awaitExecution.put(event.getId(), semaphore);
		send(event);
		return semaphore;
	}

	public void send(Event event) {
		validateData(event);
		handleSending(event);
	}

	public void receive(Event event) {
		try {
			EventBus eventBus = eventBuses.get(event.busName);
			if (eventBus != null) {
				for (EventBusListener listener : eventBus.listeners) {
					//					LOGGER.info("Listening to event " + event.id + " of type " + event.getType() + " on bus " + event.busName
					//							+ " with data " + event.data);
					listener.onEventReceived(event);
				}
			}

		} finally {
			Semaphore semaphore = awaitExecution.get(event.getId());
			if (semaphore != null) {
				semaphore.release(1);
			}
		}
	}

	private void validateData(Event event) {
		//TODO
	}

	String serialize(Object data) {
		//TODO
		return null;
	}

	String deserialize(Object data) {
		//TODO
		return null;
	}

	protected abstract void handleSending(Event event);
}
