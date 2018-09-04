package com.constellio.app.modules.rm.migrations.records;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMFolderMigrationTo8_1_0_1 extends RecordMigrationScript {

	private RMSchemasRecordsServices rm;
	private static final String FOLDER_DECOMMISSIONING_DATE = "decommissioningDate";
	private MetadataSchemasManager metadataSchemasManager;
	private String collection;

	public RMFolderMigrationTo8_1_0_1(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
	}

	@Override
	public String getSchemaType() {
		return Folder.SCHEMA_TYPE;
	}

	@Override
	public void migrate(Record record) {
		try {
			if (record.getSchemaCode().equals(Folder.DEFAULT_SCHEMA)) {
				Folder folder = rm.wrapFolder(record);
				folder.set(FOLDER_DECOMMISSIONING_DATE, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void afterLastMigratedRecord() {
		metadataSchemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder folderSchema = types.getSchema(Folder.DEFAULT_SCHEMA);
				if (folderSchema.hasMetadata(FOLDER_DECOMMISSIONING_DATE)) {
					folderSchema.deleteMetadataWithoutValidation(FOLDER_DECOMMISSIONING_DATE);
				}
			}
		});
	}
}
