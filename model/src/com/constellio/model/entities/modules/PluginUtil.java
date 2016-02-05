package com.constellio.model.entities.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.configs.SystemConfiguration;

public class PluginUtil {
	public static List<String> getDependencies(Module module) {
		return (module.getDependencies() == null) ? new ArrayList<String>() : module.getDependencies();
	}

	public static List<SystemConfiguration> getConfigurations(Module module) {
		return (module.getConfigurations() == null) ? new ArrayList<SystemConfiguration>() : module.getConfigurations();
	}

	public static Map<String, List<String>> getPermissions(Module module) {
		return (module.getPermissions() == null) ? new HashMap<String, List<String>>() : module.getPermissions();
	}

	public static List<String> getRolesForCreator(Module module) {
		return (module.getRolesForCreator() == null) ? new ArrayList<String>() : module.getRolesForCreator();
	}

}
