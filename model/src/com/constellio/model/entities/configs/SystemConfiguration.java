package com.constellio.model.entities.configs;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;

public class SystemConfiguration {

	boolean hidden;

	SystemConfigurationType type;

	String module;

	String configGroupCode;

	String code;

	String propertyKey;

	Object defaultValue;

	boolean rebootRequired;
	boolean hiddenValue;

	Class<? extends Enum<?>> enumClass;

	Class<? extends SystemConfigurationScript> scriptClass;

	SystemConfiguration(SystemConfigurationType type, String module, String configGroupCode, String code, Object defaultValue,
			Class<? extends Enum<?>> enumClass, Class<? extends SystemConfigurationScript> scriptClass, boolean hidden,
			boolean rebootRequired, boolean hiddenValue) {
		this.type = type;
		this.configGroupCode = configGroupCode;
		this.code = code;
		this.module = module;
		this.defaultValue = defaultValue;
		this.enumClass = enumClass;
		this.scriptClass = scriptClass;
		this.hidden = hidden;
		this.rebootRequired = rebootRequired;
		this.hiddenValue = hiddenValue;
		this.propertyKey = configGroupCode + "_" + code;
	}

	public SystemConfigurationType getType() {
		return type;
	}

	public String getModule() {
		return module;
	}

	public String getConfigGroupCode() {
		return configGroupCode;
	}

	public String getCode() {
		return code;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public Class<? extends Enum<?>> getEnumClass() {
		return enumClass;
	}

	public Class<? extends SystemConfigurationScript> getScriptClass() {
		return scriptClass;
	}

	public SystemConfiguration withDefaultValue(Object value) {
		return new SystemConfiguration(type, module, configGroupCode, code, value, enumClass, scriptClass, hidden, rebootRequired,
				hiddenValue);

	}

	public SystemConfiguration scriptedBy(Class<? extends SystemConfigurationScript> scriptClass) {
		return new SystemConfiguration(type, module, configGroupCode, code, defaultValue, enumClass, scriptClass, hidden,
				rebootRequired, hiddenValue);
	}

	public <T> ConfigDependency<T> dependency() {
		return new ConfigDependency<>(this);
	}

	@Override
	public int hashCode() {
		return code.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SystemConfiguration) {
			return LangUtils.isEqual(code, ((SystemConfiguration) obj).getCode());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return code;
	}

	public SystemConfiguration whichIsHidden() {
		return new SystemConfiguration(type, module, configGroupCode, code, defaultValue, enumClass, scriptClass, true,
				rebootRequired, hiddenValue);
	}

	public SystemConfiguration whichRequiresReboot() {
		return new SystemConfiguration(type, module, configGroupCode, code, defaultValue, enumClass, scriptClass, hidden,
				true, hiddenValue);
	}

	public SystemConfiguration whichHasHiddenValue() {
		return new SystemConfiguration(type, module, configGroupCode, code, defaultValue, enumClass, scriptClass, hidden,
				rebootRequired, true);
	}

	public boolean isHidden() {
		return hidden;
	}

	public boolean isHiddenValue() {
		return hiddenValue;
	}

	public boolean isRebootRequired() {
		return rebootRequired;
	}

	public String getPropertyKey() {
		return propertyKey;
	}
}


