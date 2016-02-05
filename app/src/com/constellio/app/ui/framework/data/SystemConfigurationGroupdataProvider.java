package com.constellio.app.ui.framework.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.SystemConfigurationGroupVO;
import com.constellio.app.ui.entities.SystemConfigurationVO;
import com.constellio.app.ui.framework.builders.SystemConfigurationToVOBuilder;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.services.configs.SystemConfigurationsManager;

public class SystemConfigurationGroupdataProvider implements DataProvider {
	transient private SortedMap<String, SystemConfigurationGroupVO > systemConfigurationGroupVOSortedMap;

	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		if(systemConfigurationGroupVOSortedMap == null){
			initConfigs();
		}
	}

	private void initConfigs() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		systemConfigurationGroupVOSortedMap = new TreeMap<>();
		SystemConfigurationsManager systemConfigurationsManager = constellioFactories.getModelLayerFactory()
				.getSystemConfigurationsManager();
		SystemConfigurationToVOBuilder builder = new SystemConfigurationToVOBuilder();

		List<SystemConfigurationGroup> sysConfigGroup = systemConfigurationsManager.getConfigurationGroups();
		for (SystemConfigurationGroup group : sysConfigGroup) {
			List<SystemConfigurationVO> sysConfigGroupVOList = new ArrayList<>();

			for (SystemConfiguration config : systemConfigurationsManager.getGroupConfigurationsWithCode(group.getCode())){
				if (!config.isHidden()) {
					sysConfigGroupVOList.add(builder.build(config, systemConfigurationsManager.getValue(config)));
				}
			}
			systemConfigurationGroupVOSortedMap.put(group.getCode(), new SystemConfigurationGroupVO(group.getCode(), sysConfigGroupVOList));
		}
	}

	public int size() {
		if(systemConfigurationGroupVOSortedMap == null){
			initConfigs();
		}
		return systemConfigurationGroupVOSortedMap.size();
	}

	public List<String> getCodesList() {
		if(systemConfigurationGroupVOSortedMap == null){
			initConfigs();
		}
		return new ArrayList<>(systemConfigurationGroupVOSortedMap.keySet());
	}

	public SystemConfigurationGroupVO getSystemConfigurationGroup(String code) {
		if(systemConfigurationGroupVOSortedMap == null){
			initConfigs();
		}
		return systemConfigurationGroupVOSortedMap.get(code);
	}

	public SystemConfigurationVO getValue(String groupCode, int i) {
		return systemConfigurationGroupVOSortedMap.get(groupCode).getSystemConfigurationVO(i);
	}

	public void valueChange(String groupCode, int i, Object newStringValue) {
		SystemConfigurationGroupVO groupConfig = systemConfigurationGroupVOSortedMap.get(groupCode);
		groupConfig.valueChange(i, newStringValue);
	}
}
