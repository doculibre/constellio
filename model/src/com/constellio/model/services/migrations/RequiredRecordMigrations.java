package com.constellio.model.services.migrations;

import java.util.List;

import com.constellio.model.entities.records.RecordMigrationScript;

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
