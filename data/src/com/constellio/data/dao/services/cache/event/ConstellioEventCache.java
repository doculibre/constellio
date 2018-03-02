package com.constellio.data.dao.services.cache.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.events.Event;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusListener;
import com.constellio.data.utils.ImpossibleRuntimeException;

public class ConstellioEventCache implements ConstellioCache, EventBusListener {

	private static final String PUT_EVENT_TYPE = "put";

	private static final String REMOVE_EVENT_TYPE = "remove";

	private static final String CLEAR_EVENT_TYPE = "clear";

	private String name;

	private Map<String, Object> map = new LinkedHashMap<>();

	private EventBus eventBus;

	public ConstellioEventCache(String name, EventBus eventBus) {
		this.name = name;
		this.eventBus = eventBus;
		eventBus.register(this);
	}

	@Override
	public final String getName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T get(String key) {
		return (T) map.get(key);
	}

	@Override
	public <T extends Serializable> void put(String key, T value) {
		Map<String, Object> data = new HashMap<>();
		data.put("key", key);
		data.put("value", value);
		eventBus.send(REMOVE_EVENT_TYPE, data);
	}

	@Override
	public void remove(String key) {
		HashSet<String> keys = new HashSet<>();
		keys.add(key);
		eventBus.send(REMOVE_EVENT_TYPE, keys);
	}

	@Override
	public void removeAll(Set<String> keys) {
		eventBus.send(REMOVE_EVENT_TYPE, keys);
	}

	@Override
	public void clear() {
		eventBus.send(CLEAR_EVENT_TYPE);
	}

	@Override
	public Iterator<String> keySet() {
		return map.keySet().iterator();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public List<Object> getAllValues() {
		return new ArrayList<>(map.values());
	}

	@Override
	public void onEventReceived(Event event) {
		switch (event.getType()) {
		case PUT_EVENT_TYPE:
			map.put(event.<String>getData("key"), event.getData("value"));
			break;

		case REMOVE_EVENT_TYPE:
			for (String key : event.<List<String>>getData()) {
				map.remove(key);
			}
			break;

		case CLEAR_EVENT_TYPE:
			map.clear();
			break;

		default:
			throw new ImpossibleRuntimeException("Unsupported type : " + event.getType());

		}
	}
}
