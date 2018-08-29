package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.CapsuleLanguage;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;

public class CoreMigrationTo_8_0_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.0.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		if (!Collection.SYSTEM_COLLECTION.equals(collection)) {
			Transaction transaction = new Transaction();
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());

			MetadataSchemaType metadataSchemaType = schemas.getTypes().getSchemaType(CapsuleLanguage.SCHEMA_TYPE);

			transaction.update(schemas.wrapValueListItem(schemas.getByCode(metadataSchemaType, "fr")).setTitle("Français").getWrappedRecord());

			RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
			recordServices.execute(transaction);
		}
	}
}
