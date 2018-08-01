package com.constellio.model.services.migrations;

import com.constellio.model.entities.records.RecordMigrationScript;

import java.util.List;

public class RequiredRecordMigrations {

	private long version;

	private List<RecordMigrationScript> scripts;

	public RequiredRecordMigrations(long version, List<RecordMigrationScript> scripts) {
		this.version = version;
		this.scripts = scripts;
	}

	public long getVersion() {
		return version;
	}

	public List<RecordMigrationScript> getScripts() {
		return scripts;
	}
}
