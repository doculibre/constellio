package com.constellio.app.ui.entities;

import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.entities.configs.SystemConfigurationType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import static com.constellio.model.entities.configs.SystemConfigurationType.BINARY;

public class SystemConfigurationVO implements Serializable {
	String code;
	String configSubGroupCode;
	Object value;
	SystemConfigurationType type;
	Class<? extends Enum<?>> values;
	private boolean updated;
	private String tmpFilePath;

	boolean rebootRequired;
	boolean hiddenValue;

	public SystemConfigurationVO(String code, String configSubGroupCode, Object value,
								 SystemConfigurationType type, Class<? extends Enum<?>> values, boolean rebootRequired,
								 boolean hiddenValue) {
		this.code = code;
		this.configSubGroupCode = configSubGroupCode;
		this.value = value;
		this.type = type;
		this.values = values;
		this.updated = false;
		this.rebootRequired = rebootRequired;
		this.hiddenValue = hiddenValue;
	}

	public String getCode() {
		return code;
	}

	public String getConfigSubGroupCode() {
		return configSubGroupCode;
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
					value = Integer.valueOf(stringValue.toString().replace(" ", "").replace(",", ""));
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
					final TempFileUpload tmpFile = (TempFileUpload) stringValue;
					StreamFactory<InputStream> streamFactory = new StreamFactory<InputStream>() {
						@Override
						public InputStream create(String name)
								throws IOException {
							return new FileInputStream(tmpFile.getTempFile().getPath());
						}
					};
					tmpFilePath = tmpFile.getTempFile().getPath();
					value = streamFactory;

					break;
				default:
					throw new RuntimeException("Unsupported type " + type);
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

	public boolean isHiddenValue() {
		return hiddenValue;
	}

	public void setHiddenValue(boolean hiddenValue) {
		this.hiddenValue = hiddenValue;
	}

	public void setRebootRequired(boolean rebootRequired) {
		this.rebootRequired = rebootRequired;
	}

	public boolean isRebootRequired() {
		return rebootRequired;
	}

	public void afterSetValue() {
		if (type.equals(BINARY) && tmpFilePath != null) {
			FileUtils.deleteQuietly(new File(tmpFilePath));
		}
	}
}
