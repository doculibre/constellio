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
package com.constellio.app.entities.modules;

public class Migration {

	private final String collection;

	private final String moduleId;

	private final MigrationScript script;

	public Migration(String collection, String moduleId, MigrationScript script) {
		this.collection = collection;
		this.moduleId = moduleId;
		this.script = script;
	}

	public String getModuleId() {
		return moduleId;
	}

	public MigrationScript getScript() {
		return script;
	}

	public String getVersion() {
		return script.getVersion();
	}

	public String getCollection() {
		return collection;
	}

	public String getMigrationId() {
		return collection + "_" + (moduleId == null ? "core" : moduleId) + "_" + script.getVersion();
	}

	@Override
	public String toString() {
		return "Migration{" + getMigrationId() + "}";
	}
}
