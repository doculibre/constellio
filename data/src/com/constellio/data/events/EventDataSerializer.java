package com.constellio.data.events;

public interface EventDataSerializer {

	Class<?> getSupportedDataClass();

	String serialize(Object data);

	Object deserialize(String deserialize);

}
