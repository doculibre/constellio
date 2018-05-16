package com.constellio.data.events;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_DataIsNotSerializable;

public class EventDataSerializer {

	private Map<String, EventDataSerializerExtension> serializersMappedByClassNames = new HashMap<>();
	private Map<String, EventDataSerializerExtension> serializersMappedByIds = new HashMap<>();

	public void register(EventDataSerializerExtension extension) {
		serializersMappedByClassNames.put(extension.getSupportedDataClass().getSimpleName(), extension);
		serializersMappedByIds.put(extension.getId(), extension);
	}

	public void validateData(Object data) {
		if (data instanceof Collection) {
			Iterator<Object> iterator = ((Collection) data).iterator();
			while (iterator.hasNext()) {
				validateData(iterator.next());
			}
		}

		if (data instanceof Map) {
			for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) data).entrySet()) {
				validateData(entry.getKey());
				validateData(entry.getValue());
			}
		}

		validateAtomicData(data);

	}

	private void validateAtomicData(Object data) {

		if (data != null) {

			boolean valid = false;

			if (data instanceof Serializable) {
				valid = true;
			} else {

				String className = data.getClass().getSimpleName();
				if (className.contains("TestRecord")) {
					className = "com.constellio.model.services.records.RecordImpl";
				}

				EventDataSerializerExtension extension = serializersMappedByClassNames.get(className);

				if (extension != null) {
					valid = true;
				}
			}

			if (!valid) {
				throw new EventBusManagerRuntimeException_DataIsNotSerializable(data.getClass());
			}
		}

	}

	public Object serialize(Object data) {
		if (data instanceof Collection) {
			return serializeList((Collection) data);

		} else if (data instanceof Map) {
			return serializeMap((Map) data);

		} else {
			return serializeAtomicData(data);
		}
	}

	private Collection<Object> serializeList(Collection<Object> listData) {

		Collection<Object> serializedList = createEmptyCollection(listData);

		for (Object item : listData) {
			serializedList.add(serialize(item));
		}

		return serializedList;
	}

	private Map<Object, Object> serializeMap(Map<Object, Object> mapData) {

		Map<Object, Object> serializedMap = new HashMap<>();

		for (Map.Entry<Object, Object> item : mapData.entrySet()) {
			serializedMap.put(serialize(item.getKey()), serialize(item.getValue()));
		}

		return serializedMap;
	}

	@NotNull
	private Collection<Object> createEmptyCollection(Collection<Object> listData) {
		Collection<Object> serializedList;
		if (listData instanceof List) {
			serializedList = new ArrayList<>();
		} else {
			serializedList = new HashSet<>();
		}
		return serializedList;
	}

	private Object serializeAtomicData(Object data) {
		if (data == null) {
			return null;
		}
		if (data instanceof String) {
			if (((String) data).startsWith("~")) {
				return "$~TILDE~$" + ((String) data).substring(1);
			}
		}

		String className = data.getClass().getSimpleName();
		if (className.contains("TestRecord")) {
			className = "RecordImpl";
		}

		EventDataSerializerExtension extension = serializersMappedByClassNames.get(className);
		if (extension != null) {
			return "~" + extension.getId() + ":" + extension.serialize(data);
		} else {
			try {
				serializeToBase64((Serializable) data);
			} catch (IOException e) {
				throw new RuntimeException("Cannot serialize object of class " + data.getClass(), e);
			}
			return data;
		}
	}

	public Object deserialize(Object data) {
		if (data instanceof Collection) {
			return deserializeCollection((Collection) data);

		} else if (data instanceof Map) {
			return deserializeMap((Map) data);

		} else {
			return deserializeAtomicData(data);
		}
	}

	private Collection<Object> deserializeCollection(Collection<Object> serializedList) {
		Collection<Object> deserializedList = createEmptyCollection(serializedList);

		for (Object item : serializedList) {
			deserializedList.add(deserialize(item));
		}

		return deserializedList;
	}

	private Map<Object, Object> deserializeMap(Map<Object, Object> serializedMap) {
		Map<Object, Object> deserializedMap = new HashMap<>();

		for (Map.Entry<Object, Object> item : serializedMap.entrySet()) {
			deserializedMap.put(deserialize(item.getKey()), deserialize(item.getValue()));
		}

		return deserializedMap;
	}

	private Object deserializeAtomicData(Object data) {
		if (data instanceof String) {
			if (((String) data).startsWith("~")) {
				String serializerId = StringUtils.substringBefore(((String) data).substring(1), ":");
				String serializedData = StringUtils.substringAfter(((String) data).substring(1), ":");

				EventDataSerializerExtension extension = serializersMappedByIds.get(serializerId);

				if (extension != null) {
					return extension.deserialize(serializedData);
				} else {
					return null;
				}
			}
			if (((String) data).startsWith("$~TILDE~$")) {
				return "~" + ((String) data).substring(9);
			}
		}
		return data;
	}

	/** Write the object to a Base64 string. */
	private static String serializeToBase64(Serializable o)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return DatatypeConverter.printBase64Binary(baos.toByteArray());
	}
}
