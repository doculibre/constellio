package com.constellio.data.events;

import java.util.Map;

public class Event {

	String busName;

	String type;

	String id;

	long timeStamp;

	Object data;

	public Event(String busName, String type, String id, long timeStamp, Object data) {
		this.busName = busName;
		this.type = type;
		this.id = id;
		this.timeStamp = timeStamp;
		this.data = data;
	}

	public String getBusName() {
		return busName;
	}

	public String getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public <T> T getData() {
		return (T) data;
	}

	public <T> T getData(String key) {
		Map<String, Object> map = (Map) data;
		return (T) map.get(key);
	}
}
