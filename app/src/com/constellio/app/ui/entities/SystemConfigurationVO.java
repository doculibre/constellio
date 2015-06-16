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
package com.constellio.app.ui.entities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.entities.configs.SystemConfigurationType;

public class SystemConfigurationVO implements Serializable {
	String code;
	Object value;
	SystemConfigurationType type;
	Class<? extends Enum<?>> values;
	private boolean updated;

	public SystemConfigurationVO(String code, Object value,
			SystemConfigurationType type, Class<? extends Enum<?>> values) {
		this.code = code;
		this.value = value;
		this.type = type;
		this.values = values;
		this.updated = false;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public SystemConfigurationType getType() {
		return type;
	}

	public void setType(SystemConfigurationType type) {
		this.type = type;
	}

	public Class<? extends Enum<?>> getValues() {
		return values;
	}

	public void setValues(Class<? extends Enum<?>> values) {
		this.values = values;
	}

	public void setStringValue(Object stringValue) {
		Object value = null;
		if (stringValue != null) {
			switch (type) {
			case BOOLEAN:
				value = Boolean.valueOf(stringValue.toString());
				break;
			case INTEGER:
				value = Integer.valueOf(stringValue.toString());
				break;
			case STRING:
				value = stringValue;
				break;
			case ENUM:
				for (Enum currentValue : values.getEnumConstants()) {
					if (currentValue.name().equals(stringValue)) {
						value = currentValue;
						break;
					}
				}
				break;
			case BINARY:
				//FIXME
				final TempFileUpload tmpFile = (TempFileUpload) stringValue;
				StreamFactory<InputStream> streamFactory = new StreamFactory<InputStream>() {
					@Override
					public InputStream create(String name) throws IOException {
						return new FileInputStream(tmpFile.getTempFile().getPath());
					}

				};
				value = streamFactory;
				break;
			default: throw new RuntimeException("Unsupported type " + type);
			}
		}
		setValue(value);
		setUpdated(true);
	}

	public boolean isUpdated() {
		return updated;
	}

	void setUpdated(Boolean updated) {
		this.updated = updated;
	}
}
