package com.constellio.app.modules.tasks.migrations.records;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.entities.schemas.Schemas;

public class TaskVisibilityInTreesMigrationTo8_3_1 extends RecordMigrationScript {

	private TasksSchemasRecordsServices schemas;

	public TaskVisibilityInTreesMigrationTo8_3_1(String collection, AppLayerFactory appLayerFactory) {
		this.schemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
	}

	@Override
	public String getSchemaType() {
		return Task.SCHEMA_TYPE;
	}

	@Override
	public void migrate(Record record) {
		Task task = schemas.wrapTask(record);
		task.set(Schemas.VISIBLE_IN_TREES.getLocalCode(), false);
	}

	@Override
	public void afterLastMigratedRecord() {

	}
}
