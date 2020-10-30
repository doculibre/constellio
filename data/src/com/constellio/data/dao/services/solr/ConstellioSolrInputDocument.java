package com.constellio.data.dao.services.solr;

import com.constellio.data.utils.ImpossibleRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConstellioSolrInputDocument extends SolrInputDocument {

	@Override
	public void setField(String name, Object value) {
		value = convertEmptyToNull(value);
		if (value == null) {
			super.remove(name);
		} else {
			validate(name, value);
			if (name.endsWith("_i") && (value instanceof Map)) {
				Object originalValue = ((Map) value).get("set");
				if (originalValue instanceof String && ((String) originalValue).contains(".")) {
					String convertedValue = "" + Double.valueOf((String) originalValue).intValue();
					((Map<String, String>) value).put("set", convertedValue);
				}
			}
			if (name.endsWith("_i") && (value instanceof String) && ((String) value).contains(".")) {
				value = "" + Double.valueOf((String) value).intValue();
			}
			super.setField(name, value);
		}
	}

	@Override
	public void addField(String name, Object value) {

		value = convertEmptyToNull(value);

		if (value == null) {
			super.remove(name);
		} else {
			validate(name, value);
			if (name.endsWith("_i") && (value instanceof Map)) {
				Object originalValue = ((Map) value).get("set");
				if (originalValue instanceof String && ((String) originalValue).contains(".")) {
					String convertedValue = "" + Double.valueOf((String) originalValue).intValue();
					((Map<String, String>) value).put("set", convertedValue);
				}
			}
			if (name.endsWith("_i") && (value instanceof String) && ((String) value).contains(".")) {
				value = "" + Double.valueOf((String) value).intValue();
			}
			super.addField(name, value);
		}
	}

	private Object convertEmptyToNull(Object value) {
		if (value instanceof List) {
			List newValue = new ArrayList((List) value);
			Iterator valueIterator = newValue.iterator();
			while (valueIterator.hasNext()) {
				Object currentValue = valueIterator.next();
				if (currentValue instanceof String && StringUtils.isEmpty(currentValue.toString())) {
					valueIterator.remove();
				}
			}
			return newValue;
		} else if (value instanceof Map) {
			Map newValueMap = new HashMap((Map) value);
			Set<Entry> valueList = ((Map) value).entrySet();
			Iterator<Map.Entry> valueIterator = valueList.iterator();
			while (valueIterator.hasNext()) {
				Map.Entry currentValue = valueIterator.next();
				if (currentValue.getValue() instanceof String && StringUtils.isEmpty(currentValue.getValue().toString())) {
					newValueMap.put(currentValue.getKey(), null);
				}
			}
			return newValueMap;
		} else if (value instanceof String && StringUtils.isEmpty(value.toString())) {
			return null;
		} else {
			return value;
		}
	}

	private void validate(String name, Object value) {
		if (name == null) {
			throw new ImpossibleRuntimeException("field name must not be null");
		}
		if (value == null) {
			throw new ImpossibleRuntimeException("value of field '" + name + "' must not be null");
		}
		if (name.equals("id") && !(value instanceof String)) {
			throw new ImpossibleRuntimeException("id field must be not null and a string");
		}
		if (value instanceof List) {
			for (Object item : (List) value) {
				if (item == null) {
					throw new ImpossibleRuntimeException("value of field '" + name + "' must not contain null values");
				}
			}
		}

		if (value instanceof Map) {
			for (Object item : ((Map) value).values()) {
				//				if (item == null) {
				//					throw new ImpossibleRuntimeException("value of field '" + name + "' must not contain null values");
				//				}
				if (item instanceof List) {
					for (Object mapValueItem : (List) item) {
						if (mapValueItem == null) {
							throw new ImpossibleRuntimeException("value of field '" + name + "' must not contain null values");
						}
					}
				}
			}
		}

		if (name.endsWith("_da") || name.endsWith("_dt")) {
			ensureValueIsOfClass(name, value, String.class);
		}

		if (name.endsWith("_das") || name.endsWith("_dts")) {
			ensureValueIsListOfClass(name, value, String.class);
		}
	}

	private void ensureValueIsOfClass(String fieldName, Object value, Class<?> expectedValueClass) {
		if (value instanceof Map) {
			Map<Object, Object> map = (Map) value;
			Object firstEntryValue = map.entrySet().iterator().next().getValue();
			ensureValueIsOfClass(fieldName, firstEntryValue, expectedValueClass);
		} else if (value != null && !expectedValueClass.isAssignableFrom(value.getClass())) {
			throw new ImpossibleRuntimeException(
					"value of field '" + fieldName + "' must be a " + expectedValueClass.getSimpleName() + " instead of a "
					+ value.getClass().getSimpleName());
		}

	}

	private void ensureValueIsListOfClass(String fieldName, Object value, Class<?> expectedValueClass) {
		if (value instanceof Map) {
			Map<Object, Object> map = (Map) value;
			Object firstEntryValue = map.entrySet().iterator().next().getValue();
			ensureValueIsListOfClass(fieldName, firstEntryValue, expectedValueClass);
		} else if (value != null) {
			if (!(value instanceof List)) {
				throw new ImpossibleRuntimeException(
						"value of field '" + fieldName + "' must be a List, but is of class " + value.getClass());
			}
			List<Object> list = (List) value;
			for (Object item : list) {
				ensureValueIsOfClass(fieldName, item, expectedValueClass);
			}
		}

	}
}
