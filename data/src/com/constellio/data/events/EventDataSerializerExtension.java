package com.constellio.data.events;

public interface EventDataSerializerExtension {

	Class<?> getSupportedDataClass();

	String serialize(Object data);

	Object deserialize(String deserialize);

}
