package com.constellio.model.entities.records;

public abstract class RecordMigrationScript {

	public String getId() {
		return getClass().getSimpleName();
	}

	public abstract String getSchemaType();

	public abstract void migrate(Record record);

	public void afterLastMigratedRecord() {

	}
}
