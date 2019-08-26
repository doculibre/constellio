package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.*;

public class SystemConfigurationGroupVO implements Serializable {

	String groupCode;

	List<SystemConfigurationVO> configs;

	List<Integer> updatedSystemConfigurationVOIndexes = new ArrayList<>();

	public SystemConfigurationGroupVO(String groupCode, List<SystemConfigurationVO> configs) {

		this.groupCode = groupCode;
		this.configs = configs;
//		this.configs.sort(new Comparator<SystemConfigurationVO>() {
//			@Override
//			public int compare(SystemConfigurationVO config1, SystemConfigurationVO config2) {
//				return config1.getConfigSubGroupCode().compareTo(config2.getConfigSubGroupCode());
//			}
//		});
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

	public List<String> getConfigSubGroupCodes() {
		Set<String> configsSubGroupCodes = new LinkedHashSet<>();
		for(SystemConfigurationVO config : configs){
			configsSubGroupCodes.add(config.getConfigSubGroupCode());
		}

		return new ArrayList<>(configsSubGroupCodes);
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

	public List<SystemConfigurationVO> getSystemConfigurationVOsForSubGroup(String configSubGroupCode) {
		ArrayList<SystemConfigurationVO> configsForSubGroup = new ArrayList<>();
		for(SystemConfigurationVO config : configs){
			if(config.getConfigSubGroupCode().equals(configSubGroupCode)) {
				configsForSubGroup.add(config);
			}
		}

		return configsForSubGroup;
	}

	public SystemConfigurationVO getSystemConfigurationVO(String code) {
		for(Integer index : updatedSystemConfigurationVOIndexes){
			SystemConfigurationVO systemConfigurationVO = configs.get(index);
			if(systemConfigurationVO.getCode().equals(code)) {
				return systemConfigurationVO;
			}
		}

		return null;
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
