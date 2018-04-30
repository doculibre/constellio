package com.constellio.data.events;

import java.util.ArrayList;
import java.util.List;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.utils.TimeProvider;

public class EventBus {

	String name;

	EventBusManager manager;

	List<EventBusListener> listeners = new ArrayList<>();

	EventBusEventsExecutionStrategy executionStrategy;

	public EventBus(String name, EventBusManager manager, EventBusEventsExecutionStrategy executionStrategy) {
		this.name = name;
		this.manager = manager;
		this.executionStrategy = executionStrategy;
	}

	public void send(String type) {
		this.send(type, null);
	}

	public void send(String type, Object data) {
		long timeStamp = TimeProvider.getLocalDateTime().toDate().getTime();
		send(type, data, timeStamp);
	}

	void send(String type, Object data, long timeStamp) {
		manager.send(new Event(name, type, UUIDV1Generator.newRandomId(), timeStamp, data), executionStrategy);
	}

	public void register(EventBusListener listener) {
		listeners.add(listener);
	}

	public void unregister(EventBusListener listener) {
		listeners.add(listener);
	}

	public String getName() {
		return name;
	}
}
