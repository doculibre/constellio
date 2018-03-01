package com.constellio.data.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;

public class EventBus {

	String name;

	EventBusManager manager;

	List<EventBusListener> listeners = new ArrayList<>();

	public EventBus(String name, EventBusManager manager) {
		this.name = name;
		this.manager = manager;
	}

	public void send(String type, Object data) {
		long timeStamp = new Date().getTime();
		manager.send(new Event(name, type, UUIDV1Generator.newRandomId(), timeStamp, data));
	}

	public void send(String type) {
		this.send(type, null);
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
