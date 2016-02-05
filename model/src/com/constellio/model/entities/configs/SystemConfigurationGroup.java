package com.constellio.model.entities.configs;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SystemConfigurationGroup {

	String module;

	String groupCode;

	public SystemConfigurationGroup(String module, String groupCode) {
		this.module = module;
		this.groupCode = groupCode;
	}

	public SystemConfiguration createString(String code) {
		return new SystemConfiguration(SystemConfigurationType.STRING, module, groupCode, code, null, null, null, false);
	}

	public SystemConfiguration createString(String code, String defaultValue) {
		return new SystemConfiguration(SystemConfigurationType.STRING, module, groupCode, code, defaultValue, null, null, false);
	}

	public SystemConfiguration createEnum(String code, Class<? extends Enum<?>> enumClass) {
		return new SystemConfiguration(SystemConfigurationType.ENUM, module, groupCode, code, null, enumClass, null, false);
	}

	public SystemConfiguration createInteger(String code) {
		return new SystemConfiguration(SystemConfigurationType.INTEGER, module, groupCode, code, null, null, null, false);
	}

	public SystemConfiguration createBinary(String code) {
		return new SystemConfiguration(SystemConfigurationType.BINARY, module, groupCode, code, null, null, null, false);
	}

	public SystemConfiguration createBooleanTrueByDefault(String code) {
		return new SystemConfiguration(SystemConfigurationType.BOOLEAN, module, groupCode, code, true, null, null, false);
	}

	public SystemConfiguration createBooleanFalseByDefault(String code) {
		return new SystemConfiguration(SystemConfigurationType.BOOLEAN, module, groupCode, code, false, null, null, false);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return groupCode;
	}

	public String getCode() {
		return groupCode;
	}
}
