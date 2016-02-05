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

	public void valueChange(Integer i, Object newStringValue) {
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
