package com.constellio.app.modules.tasks.migrations.records;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.entities.schemas.Schemas;

public class TaskVisibilityInTreesMigrationTo8_3_1 extends RecordMigrationScript {

	private RMSchemasRecordsServices rm;

	public TaskVisibilityInTreesMigrationTo8_3_1(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	@Override
	public String getSchemaType() {
		return Task.SCHEMA_TYPE;
	}

	@Override
	public void migrate(Record record) {
		try {
			RMTask task = rm.wrapRMTask(record);
			task.set(Schemas.VISIBLE_IN_TREES.getLocalCode(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void afterLastMigratedRecord() {

	}
}
