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
package com.constellio.app.services.migrations;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.app.entities.modules.Migration;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.utils.DependencyUtils;

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
		int result = versionsComparator.compare(migration1Version, migration2Version);
		if (result == 0) {
			Integer script1ModuleIndex = modulesInDependencyOrder.indexOf(migration1.getModuleId());
			Integer script2ModuleIndex = modulesInDependencyOrder.indexOf(migration2.getModuleId());
			result = script1ModuleIndex.compareTo(script2ModuleIndex);
		}
		return result;
	}

	public static MigrationScriptsComparator forModules(List<Module> modules) {
		Map<String, Set<String>> dependencies = new HashMap<>();
		for (Module module : modules) {
			dependencies.put(module.getId(), new HashSet<>(module.getDependencies()));
		}

		List<String> modulesInDependencyOrder = new DependencyUtils<String>().sortByDependency(dependencies, null, false);
		return new MigrationScriptsComparator(modulesInDependencyOrder);
	}

}
