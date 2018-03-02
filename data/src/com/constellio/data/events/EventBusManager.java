package com.constellio.data.events;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_EventBusAlreadyExist;
import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_NoSuchEventBus;

public class EventBusManager implements EventReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventBusManager.class);

	protected Map<String, EventBus> eventBuses = new HashMap<>();

	protected EventBusSendingService eventBusSendingService;

	protected EventDataSerializer eventDataSerializer = new EventDataSerializer();

	public EventBusManager(EventBusSendingService eventBusSendingService) {
		this.eventBusSendingService = eventBusSendingService;
		this.eventBusSendingService.setEventReceiver(this);
	}

	public EventBusManager setEventBusSendingService(EventBusSendingService eventBusSendingService) {
		this.eventBusSendingService = eventBusSendingService;
		this.eventBusSendingService.setEventReceiver(this);
		return this;
	}

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

	public void send(Event event) {
		eventDataSerializer.validateData(event.getData());
		receive(event);
		eventBusSendingService.sendRemotely(event);

	}

	public void receive(Event event) {
		EventBus eventBus = eventBuses.get(event.busName);
		if (eventBus != null) {
			for (EventBusListener listener : eventBus.listeners) {
				listener.onEventReceived(event);
			}
		}

	}

}
