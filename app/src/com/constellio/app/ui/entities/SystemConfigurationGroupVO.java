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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SystemConfigurationGroupVO implements Serializable {

	String groupCode;

	List<SystemConfigurationVO> configs = new ArrayList<>();

	List<Integer> updatedSystemConfigurationVOIndexes = new ArrayList<>();

	public SystemConfigurationGroupVO(String groupCode, List<SystemConfigurationVO> configs) {

		this.groupCode = groupCode;
		this.configs = configs;
	}

	public String getGroupCode() {
		return groupCode;
	}

	public void setGroupCode(String groupCode) {
		this.groupCode = groupCode;
	}

	public List<SystemConfigurationVO> getConfigs() {
		return configs;
	}

	public void setConfigs(List<SystemConfigurationVO> configs) {
		this.configs = configs;
	}

	public SystemConfigurationVO getSystemConfigurationVO(int i) {
		return configs.get(i);
	}

	public void valueChange(Integer i, String newStringValue) {
		SystemConfigurationVO config = configs.get(i);
		config.setStringValue(newStringValue);
		updatedSystemConfigurationVOIndexes.add(i);
	}

	public boolean isUpdated() {
		return updatedSystemConfigurationVOIndexes.size() != 0;
	}

	public void valueSave(Integer i) {
		SystemConfigurationVO config = configs.get(i);
		config.setUpdated(false);
		updatedSystemConfigurationVOIndexes.remove(i);
	}

}
