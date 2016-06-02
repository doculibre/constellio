package com.constellio.app.modules.es.migrations;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESMigrationCombo implements ComboMigrationScript {
	@Override
	public List<MigrationScript> getVersions() {
		List<MigrationScript> scripts = new ArrayList<>();

		scripts.add(new ESMigrationTo5_1_6());
		scripts.add(new ESMigrationTo6_1());
		scripts.add(new ESMigrationTo6_2());
		scripts.add(new ESMigrationTo6_3());

		return scripts;
	}

	@Override
	public String getVersion() {
		return getVersions().get(getVersions().size() - 1).getVersion();
	}

	GeneratedESMigrationCombo generatedComboMigration;

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		generatedComboMigration = new GeneratedESMigrationCombo(collection, appLayerFactory,
				migrationResourcesProvider);

		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();
		generatedComboMigration.applyGeneratedRoles();
		generatedComboMigration.applySchemasDisplay(appLayerFactory.getMetadataSchemasDisplayManager());
		applySchemasDisplay2(collection, migrationResourcesProvider, appLayerFactory.getMetadataSchemasDisplayManager());

		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		MetadataSchemaTypes types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		recordServices.execute(createRecordTransaction(collection, migrationResourcesProvider, appLayerFactory, types));
	}

	private void applySchemasDisplay2(String collection, MigrationResourcesProvider migrationResourcesProvider,
			SchemasDisplayManager manager) {
		Map<String, Map<Language, String>> groups = migrationResourcesProvider.getLanguageMapWithKeys(asList("default"));
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);

		//transaction.add(manager.getType(collection, ConnectorHttpDocument.SCHEMA_TYPE).withMetadataGroup(groups));

		manager.execute(transaction.build());
	}

	private Transaction createRecordTransaction(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory, MetadataSchemaTypes types) {
		Transaction transaction = new Transaction();

		return transaction;
	}

	class SchemaAlteration extends MetadataSchemasAlterationHelper {

		protected SchemaAlteration(String collection,
				MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			generatedComboMigration.applyGeneratedSchemaAlteration(typesBuilder);
		}

	}

}
