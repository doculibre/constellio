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
		return new SystemConfiguration(SystemConfigurationType.STRING, module, groupCode, code, null, null, null);
	}

	public SystemConfiguration createString(String code, String defaultValue) {
		return new SystemConfiguration(SystemConfigurationType.STRING, module, groupCode, code, defaultValue, null, null);
	}

	public SystemConfiguration createEnum(String code, Class<? extends Enum<?>> enumClass) {
		return new SystemConfiguration(SystemConfigurationType.ENUM, module, groupCode, code, null, enumClass, null);
	}

	public SystemConfiguration createInteger(String code) {
		return new SystemConfiguration(SystemConfigurationType.INTEGER, module, groupCode, code, null, null, null);
	}

	public SystemConfiguration createBinary(String code) {
		return new SystemConfiguration(SystemConfigurationType.BINARY, module, groupCode, code, null, null, null);
	}


	public SystemConfiguration createBooleanTrueByDefault(String code) {
		return new SystemConfiguration(SystemConfigurationType.BOOLEAN, module, groupCode, code, true, null, null);
	}

	public SystemConfiguration createBooleanFalseByDefault(String code) {
		return new SystemConfiguration(SystemConfigurationType.BOOLEAN, module, groupCode, code, false, null, null);
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
