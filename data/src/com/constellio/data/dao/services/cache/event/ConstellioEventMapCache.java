package com.constellio.data.dao.services.cache.event;

import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheOptions;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.events.Event;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusListener;
import com.constellio.data.utils.ImpossibleRuntimeException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConstellioEventMapCache implements ConstellioCache, EventBusListener {

	private static final String PUT_EVENT_TYPE = "put";

	private static final String REMOVE_EVENT_TYPE = "remove";

	private static final String CLEAR_EVENT_TYPE = "clear";

	private String name;

	private Map<String, Object> map = new LinkedHashMap<>();

	private EventBus eventBus;

	ConstellioCacheOptions options;

	public ConstellioEventMapCache(String name, EventBus eventBus, ConstellioCacheOptions options) {
		this.name = name;
		this.eventBus = eventBus;
		this.options = options;
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
	public <T extends Serializable> void put(String key, T value, InsertionReason insertionReason) {
		if (insertionReason == InsertionReason.WAS_OBTAINED) {
			map.put(key, value);

		} else if (insertionReason == InsertionReason.WAS_MODIFIED) {

			if (options.isInvalidateRemotelyWhenPutting()) {

				//Disabled for test purposes
				//map.put(key, value);
				map.remove(key); //Added for test purposes

				HashSet<String> keys = new HashSet<>();
				keys.add(key);
				eventBus.send(REMOVE_EVENT_TYPE, keys);

			} else {
				map.put(key, value);
				Map<String, Object> params = new HashMap<>();
				params.put("key", key);
				params.put("value", value);

				eventBus.send(PUT_EVENT_TYPE, params);

			}
		}
	}

	@Override
	public void remove(String key) {
		HashSet<String> keys = new HashSet<>();
		keys.add(key);
		removeAll(keys);
	}

	@Override
	public void removeAll(Set<String> keys) {
		for (String key : keys) {
			map.remove(key);
		}
		eventBus.send(REMOVE_EVENT_TYPE, keys);
	}

	@Override
	public void clear() {
		map.clear();
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
	public void setOptions(ConstellioCacheOptions options) {
		this.options = options;
	}

	@Override
	public void onEventReceived(Event event) {
		switch (event.getType()) {
			case PUT_EVENT_TYPE:
				map.put(event.<String>getData("key"), event.getData("value"));
				break;

			case REMOVE_EVENT_TYPE:
				for (String key : event.<Set<String>>getData()) {
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

	@Override
	public void close() throws Exception {

	}
}
