package com.constellio.data.events;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;

import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_DataIsNotSerializable;

public class EventDataSerializer {

	private Map<String, EventDataSerializerExtension> serializersMappedByClassNames = new HashMap<>();
	private Map<String, EventDataSerializerExtension> serializersMappedByIds = new HashMap<>();

	public void register(EventDataSerializerExtension extension) {
		serializersMappedByClassNames.put(extension.getSupportedDataClass().getSimpleName(), extension);
		serializersMappedByIds.put(extension.getId(), extension);
	}

	public void validateData(Object data) {

		validateAtomicData(data);
	}

	private void validateAtomicData(Object data) {

		if (data != null) {

			boolean valid = false;

			if (data instanceof Serializable) {
				valid = true;
			} else {
				EventDataSerializerExtension extension = serializersMappedByClassNames.get(data.getClass().getSimpleName());
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
		return serializeAtomicData(data);
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

		EventDataSerializerExtension extension = serializersMappedByClassNames.get(data.getClass().getSimpleName());
		if (extension != null) {
			return "~" + extension.getId() + ":" + extension.serialize(data);
		} else {
			return data;
		}
	}

	public Object deserialize(Object data) {
		return deserializeAtomicData(data);
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

	/** Read the object from Base64 string. */
	private static Object deserializeBase64(String s)
			throws IOException,
			ClassNotFoundException {
		byte[] data = DatatypeConverter.parseBase64Binary(s);
		ObjectInputStream ois = new ObjectInputStream(
				new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;
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
