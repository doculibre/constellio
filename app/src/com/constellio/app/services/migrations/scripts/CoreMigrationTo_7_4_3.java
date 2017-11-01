package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.core.CoreTypes;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.calculators.JEXLMetadataValueCalculator;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_4_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.4.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor7_4_3(collection, migrationResourcesProvider, appLayerFactory).migrate();

		SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		for (MetadataSchema schema : CoreTypes.coreSchemas(appLayerFactory, collection)) {
			if (schema.hasMetadataWithCode(Schemas.PATH.getLocalCode()) && !SavedSearch.DEFAULT_SCHEMA.equals(schema.getCode())) {
				transaction.add(metadataSchemasDisplayManager.getMetadata(collection,
						schema.get(Schemas.PATH.getLocalCode()).getCode()).withVisibleInAdvancedSearchStatus(true));
			}
		}
		metadataSchemasDisplayManager.execute(transaction);
	}

	private class CoreSchemaAlterationFor7_4_3 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor7_4_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			for (MetadataBuilder metadata : typesBuilder.getAllCalculatedMetadatas()) {
				if (metadata.getInheritance() == null
						&& metadata.getDataEntry().getType().equals(DataEntryType.CALCULATED)
						&& ((CalculatedDataEntry) metadata.getDataEntry())
						.getCalculator() instanceof JEXLMetadataValueCalculator) {
					MetadataValueCalculator<?> calculator = ((CalculatedDataEntry) metadata.getDataEntry()).getCalculator();
					String currentScript = ((JEXLMetadataValueCalculator) calculator).getExpression();
					metadata.defineDataEntry().asJexlScript("#STRICT:" + currentScript);
				}
			}
		}
	}
}
