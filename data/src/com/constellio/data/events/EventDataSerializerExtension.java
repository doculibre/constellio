package com.constellio.data.events;

public interface EventDataSerializerExtension {

	String getId();

	Class<?> getSupportedDataClass();

	String serialize(Object data);

	Object deserialize(String deserialize);

}
