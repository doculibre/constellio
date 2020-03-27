package com.constellio.data.events;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_EventBusAlreadyExist;
import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_NoSuchEventBus;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.data.events.EventBusEventsExecutionStrategy.EXECUTED_LOCALLY_THEN_SENT_REMOTELY;
import static com.constellio.data.events.EventBusEventsExecutionStrategy.ONLY_SENT_REMOTELY;

public class EventBusManager implements EventReceiver, StatefulService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventBusManager.class);

	protected Map<String, EventBus> eventBuses = new HashMap<>();

	protected EventBusSendingService eventBusSendingService;

	protected EventDataSerializer eventDataSerializer = new EventDataSerializer();

	protected DataLayerSystemExtensions extensions;

	protected boolean paused;

	public EventBusManager(EventBusSendingService eventBusSendingService, DataLayerSystemExtensions extensions) {
		this.extensions = extensions;
		setEventBusSendingService(eventBusSendingService);
		this.paused = true;
	}

	public EventBusManager setEventBusSendingService(EventBusSendingService eventBusSendingService) {
		this.eventBusSendingService = eventBusSendingService;
		this.eventBusSendingService.setEventReceiver(this);
		this.eventBusSendingService.setEventDataSerializer(eventDataSerializer);
		this.eventBusSendingService.start(paused);
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

	public EventBus createEventBus(String name, EventBusEventsExecutionStrategy executionStrategy) {
		EventBus eventBus = eventBuses.get(name);
		if (eventBus != null) {
			throw new EventBusManagerRuntimeException_EventBusAlreadyExist(name);
		}
		eventBus = new EventBus(name, this, executionStrategy);
		eventBuses.put(name, eventBus);
		return eventBus;
	}

	public void send(Event event, EventBusEventsExecutionStrategy executionStrategy) {
		if (!paused) {
			extensions.onEventSent(event);
			if (executionStrategy == EXECUTED_LOCALLY_THEN_SENT_REMOTELY) {
				eventDataSerializer.validateData(event.getData());
				receive(event, false);
				eventBusSendingService.sendRemotely(event);

			} else if (executionStrategy == ONLY_SENT_REMOTELY) {
				eventDataSerializer.validateData(event.getData());
				eventBusSendingService.sendRemotely(event);

			}
		}
	}

	public void receive(Event event) {
		receive(event, true);
	}

	public void receive(Event event, boolean remoteEvent) {
		extensions.onEventReceived(event, remoteEvent);
		EventBus eventBus = eventBuses.get(event.busName);
		if (eventBus != null) {
			for (EventBusListener listener : eventBus.listeners) {
				listener.onEventReceived(event);
			}
		}

	}

	public EventDataSerializer getEventDataSerializer() {
		return eventDataSerializer;
	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {
		if (eventBusSendingService != null) {
			this.eventBusSendingService.close();
		}
	}

	public void pause() {
		this.paused = true;
		if (eventBusSendingService != null) {
			eventBusSendingService.pause();
		}
	}

	public void resume() {
		this.paused = false;
		if (eventBusSendingService != null) {
			eventBusSendingService.resume();
		}
	}
}
