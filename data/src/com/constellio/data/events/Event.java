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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Event))
			return false;

		Event event = (Event) o;

		if (timeStamp != event.timeStamp)
			return false;
		if (busName != null ? !busName.equals(event.busName) : event.busName != null)
			return false;
		if (type != null ? !type.equals(event.type) : event.type != null)
			return false;
		if (id != null ? !id.equals(event.id) : event.id != null)
			return false;
		return data != null ? data.equals(event.data) : event.data == null;
	}

	@Override
	public int hashCode() {
		int result = busName != null ? busName.hashCode() : 0;
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (id != null ? id.hashCode() : 0);
		result = 31 * result + (int) (timeStamp ^ (timeStamp >>> 32));
		result = 31 * result + (data != null ? data.hashCode() : 0);
		return result;
	}
}
