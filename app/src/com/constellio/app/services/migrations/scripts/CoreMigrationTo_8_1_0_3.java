package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.ValueListServices;
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

public class CoreMigrationTo_8_1_0_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1.0.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		if (!Collection.SYSTEM_COLLECTION.equals(collection)) {

			new SchemaAlterationsFor8_1_0_3(collection, migrationResourcesProvider, appLayerFactory).migrate();

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

	}

	public static void createDefaultCapsuleLanguages(MigrationResourcesProvider migrationResourcesProvider,
													 Transaction transaction, SchemasRecordsServices schemas) {
		transaction.add(schemas.newValueListItem("ddvCapsuleLanguage_default").setCode("fr")
				.setTitles(migrationResourcesProvider.getLanguagesString("languages.fr")));
		transaction.add(schemas.newValueListItem("ddvCapsuleLanguage_default").setCode("en")
				.setTitles(migrationResourcesProvider.getLanguagesString("languages.en")));
	}


	private static class SchemaAlterationsFor8_1_0_3 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationsFor8_1_0_3(String collection, MigrationResourcesProvider provider,
											  AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			if (!builder.getSchemaType(TemporaryRecord.SCHEMA_TYPE).hasSchema(VaultScanReport.SCHEMA)) {
				MetadataSchemaBuilder schema = typesBuilder.getSchemaType(TemporaryRecord.SCHEMA_TYPE)
						.createCustomSchema(VaultScanReport.SCHEMA);
				schema.createUndeletable(VaultScanReport.NUMBER_OF_DELETED_CONTENTS).setType(MetadataValueType.NUMBER)
						.setSystemReserved(true);
				schema.createUndeletable(VaultScanReport.MESSAGE).setType(MetadataValueType.TEXT).setSystemReserved(true);
			}

			if (!builder.hasSchemaType(CapsuleLanguage.SCHEMA_TYPE)) {
				Map<Language, String> titles = migrationResourcesProvider.getLanguagesString("init.ddvCapsuleLanguage");
				ValueListServices.CreateValueListOptions options = new ValueListServices.CreateValueListOptions();
				options.setMultilingual(true);

				new ValueListServices(appLayerFactory, collection)
						.createValueDomain(CapsuleLanguage.SCHEMA_TYPE, titles, options, typesBuilder);
				MetadataSchemaBuilder capsuleSchema = typesBuilder.getSchema(Capsule.DEFAULT_SCHEMA);
				capsuleSchema.create(Capsule.LANGUAGE).defineReferencesTo(typesBuilder.getSchemaType(CapsuleLanguage.SCHEMA_TYPE));
				capsuleSchema.create(Capsule.IMAGES).setType(CONTENT).setMultivalue(true);
			}
		}
	}
}