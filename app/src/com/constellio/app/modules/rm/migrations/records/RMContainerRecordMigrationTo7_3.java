package com.constellio.app.modules.rm.migrations.records;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMContainerRecordMigrationTo7_3 extends RecordMigrationScript {

	AppLayerFactory appLayerFactory;
	MetadataSchemasManager metadataSchemasManager;
	String collection;
	RMSchemasRecordsServices rm;

	public RMContainerRecordMigrationTo7_3(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public String getSchemaType() {
		return ContainerRecord.SCHEMA_TYPE;
	}

	@Override
	public void migrate(Record record) {

		ContainerRecord container = rm.wrapContainerRecord(record);
		List<String> administrativeUnits = container.getAdministrativeUnits();
		String administrativeUnit = container.get("administrativeUnit");

		if (administrativeUnit != null && !administrativeUnits.contains(administrativeUnit)) {
			List<String> newAdministrativeUnits = new ArrayList<>(administrativeUnits);
			newAdministrativeUnits.add(administrativeUnit);
			container.setAdministrativeUnits(newAdministrativeUnits);
		}
		container.set("administrativeUnit", null);

	}

	@Override
	public void afterLastMigratedRecord() {
		metadataSchemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder containerRecordSchema = types.getSchema(ContainerRecord.DEFAULT_SCHEMA);
				containerRecordSchema.deleteMetadataWithoutValidation("administrativeUnit");
			}
		});
	}
}
