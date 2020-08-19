package com.constellio.app.services.migrations;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.Migration;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.modules.PluginUtil;
import com.constellio.model.utils.DependencyUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MigrationScriptsComparator implements Comparator<Migration> {

	private List<String> modulesInDependencyOrder;

	private VersionsComparator versionsComparator;

	public MigrationScriptsComparator(List<String> modulesInDependencyOrder) {
		this.modulesInDependencyOrder = modulesInDependencyOrder;
		this.versionsComparator = new VersionsComparator();
	}

	@Override
	public int compare(Migration migration1, Migration migration2) {
		String migration1Version = migration1.getVersion();
		String migration2Version = migration2.getVersion();

		if (migration1.getScript() instanceof ComboMigrationScript) {
			migration1Version = "1.0";
		}

		if (migration2.getScript() instanceof ComboMigrationScript) {
			migration2Version = "1.0";
		}

		int result = versionsComparator.compare(migration1Version, migration2Version);
		if (result == 0) {
			Integer script1ModuleIndex = modulesInDependencyOrder.indexOf(migration1.getModuleId());
			Integer script2ModuleIndex = modulesInDependencyOrder.indexOf(migration2.getModuleId());
			result = script1ModuleIndex.compareTo(script2ModuleIndex);
		}
		return result;
	}

	public static MigrationScriptsComparator forModules(List<? extends Module> modules) {
		Map<String, Set<String>> dependencies = new HashMap<>();
		for (Module module : modules) {
			dependencies.put(module.getId(), new HashSet<>(getDependencies(module)));
		}

		List<String> modulesInDependencyOrder = new DependencyUtils<String>().sortByDependency(dependencies);
		return new MigrationScriptsComparator(modulesInDependencyOrder);
	}

	private static List<String> getDependencies(Module module) {
		return PluginUtil.getDependencies(module);
	}

}
