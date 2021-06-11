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

	public String getLegacyMigrationId() {
		return collection + "_" + (moduleId == null ? "core" : moduleId) + "_" + script.getVersion();
	}

	@Override
	public String toString() {
		return "Migration{" + script.getClass().getSimpleName() + "}";
	}
}
