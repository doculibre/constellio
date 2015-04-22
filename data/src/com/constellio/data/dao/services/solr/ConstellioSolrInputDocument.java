/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.dao.services.solr;

import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;

import com.constellio.data.utils.ImpossibleRuntimeException;

public class ConstellioSolrInputDocument extends SolrInputDocument {

	@Override
	public void setField(String name, Object value) {
		validate(name, value);
		super.setField(name, value);
	}

	@Override
	public void setField(String name, Object value, float boost) {
		validate(name, value);
		super.setField(name, value, boost);
	}

	@Override
	public void addField(String name, Object value, float boost) {
		validate(name, value);
		super.addField(name, value, boost);
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
				if (item == null) {
					throw new ImpossibleRuntimeException("value of field '" + name + "' must not contain null values");
				}
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
