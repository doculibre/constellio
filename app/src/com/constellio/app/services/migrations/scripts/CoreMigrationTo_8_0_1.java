package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.modules.rm.services.ValueListServices.CreateValueListOptions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.CapsuleLanguage;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.VaultScanReport;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;

public class CoreMigrationTo_8_0_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		if (!Collection.SYSTEM_COLLECTION.equals(collection)) {

			new CoreSchemaAlterationFor_8_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

			Transaction transaction = new Transaction();
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
			createDefaultCapsuleLanguages(migrationResourcesProvider, transaction, schemas);

			RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
			recordServices.execute(transaction);

			SchemasDisplayManager schemaDisplaysManager = appLayerFactory.getMetadataSchemasDisplayManager();
			schemaDisplaysManager
					.saveMetadata(schemaDisplaysManager.getMetadata(collection, Capsule.DEFAULT_SCHEMA + "_" + Capsule.LANGUAGE)
							.withInputType(MetadataInputType.DROPDOWN));
			schemaDisplaysManager
					.saveMetadata(schemaDisplaysManager.getMetadata(collection, Capsule.DEFAULT_SCHEMA + "_" + Capsule.IMAGES)
							.withInputType(MetadataInputType.CONTENT));

			SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
			displayManager.saveSchema(displayManager.getSchema(collection, VaultScanReport.FULL_SCHEMA).withNewTableMetadatas(
					VaultScanReport.FULL_SCHEMA + "_" + VaultScanReport.NUMBER_OF_DELETED_CONTENTS,
					VaultScanReport.FULL_SCHEMA + "_" + VaultScanReport.MESSAGE));
		}

		//new CoreSchemaAlterationFor_8_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

	}

	public static void createDefaultCapsuleLanguages(MigrationResourcesProvider migrationResourcesProvider,
													 Transaction transaction, SchemasRecordsServices schemas) {
		transaction.add(schemas.newValueListItem("ddvCapsuleLanguage_default").setCode("fr")
				.setTitles(migrationResourcesProvider.getLanguagesString("languages.fr")));
		transaction.add(schemas.newValueListItem("ddvCapsuleLanguage_default").setCode("en")
				.setTitles(migrationResourcesProvider.getLanguagesString("languages.en")));
	}

	class CoreSchemaAlterationFor_8_0_1 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_0_1(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			Map<Language, String> titles = migrationResourcesProvider.getLanguagesString("init.ddvCapsuleLanguage");

			CreateValueListOptions options = new CreateValueListOptions();
			options.setMultilingual(true);

			new ValueListServices(appLayerFactory, collection)
					.createValueDomain(CapsuleLanguage.SCHEMA_TYPE, titles, options, typesBuilder);

			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(TemporaryRecord.SCHEMA_TYPE)
					.createCustomSchema(VaultScanReport.SCHEMA);
			schema.createUndeletable(VaultScanReport.NUMBER_OF_DELETED_CONTENTS).setType(MetadataValueType.NUMBER)
					.setSystemReserved(true);
			schema.createUndeletable(VaultScanReport.MESSAGE).setType(MetadataValueType.TEXT).setSystemReserved(true);

			MetadataSchemaBuilder capsuleSchema = typesBuilder.getSchema(Capsule.DEFAULT_SCHEMA);
			capsuleSchema.create(Capsule.LANGUAGE).defineReferencesTo(typesBuilder.getSchemaType(CapsuleLanguage.SCHEMA_TYPE));
			capsuleSchema.create(Capsule.IMAGES).setType(CONTENT).setMultivalue(true);
		}
	}
}
