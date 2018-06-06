package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_8_0_1 implements MigrationScript {
    @Override
    public String getVersion() {
        return "8.0.1";
    }

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		if (!Collection.SYSTEM_COLLECTION.equals(collection)) {
			new ValueListServices(appLayerFactory, collection).createValueDomain("ddvCapsuleLanguage", "Langue de la capsule");
			new CoreSchemaAlterationFor_8_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
			
			Transaction transaction = new Transaction();
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
			transaction.add(schemas.newValueListItem("ddvCapsuleLanguage_default").setCode("fr").setTitle("Français"));
			transaction.add(schemas.newValueListItem("ddvCapsuleLanguage_default").setCode("en").setTitle("Anglais"));
			
			RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
			recordServices.execute(transaction);

			SchemasDisplayManager schemaDisplaysManager = appLayerFactory.getMetadataSchemasDisplayManager();
			schemaDisplaysManager.saveMetadata(schemaDisplaysManager.getMetadata(collection, Capsule.DEFAULT_SCHEMA + "_" + Capsule.LANGUAGE)
					.withInputType(MetadataInputType.DROPDOWN));
			schemaDisplaysManager.saveMetadata(schemaDisplaysManager.getMetadata(collection, Capsule.DEFAULT_SCHEMA + "_" + Capsule.IMAGES)
					.withInputType(MetadataInputType.CONTENT));
		}
	}

	class CoreSchemaAlterationFor_8_0_1 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder capsuleSchema = typesBuilder.getSchema(Capsule.DEFAULT_SCHEMA);
			capsuleSchema.create(Capsule.LANGUAGE).defineReferencesTo(typesBuilder.getSchemaType("ddvCapsuleLanguage"));
			capsuleSchema.create(Capsule.IMAGES).setType(CONTENT).setMultivalue(true);
		}
	}
}
