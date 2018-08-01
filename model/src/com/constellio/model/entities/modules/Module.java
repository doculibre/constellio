package com.constellio.model.entities.modules;

import com.constellio.model.entities.configs.SystemConfiguration;

import java.util.List;
import java.util.Map;

public interface Module extends ConstellioPlugin {
	boolean isComplementary();

	List<String> getDependencies();

	List<SystemConfiguration> getConfigurations();

	Map<String, List<String>> getPermissions();

	List<String> getRolesForCreator();

}
