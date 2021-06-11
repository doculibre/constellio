package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.ArrayList;
import java.util.List;

public class RMMigrationTo9_2_13 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.2.13";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_2_13(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}


	private class SchemaAlterationFor9_2_13 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_2_13(String collection, MigrationResourcesProvider migrationResourcesProvider,
								  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypes types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
			SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
			for (MetadataSchemaType schemaType : types.getSchemaTypes()) {
				for (MetadataSchemaBuilder schemaBuilder : typesBuilder.getSchemaType(schemaType.getCode()).getAllSchemas()) {
					setSummaryForSchema(schemaBuilder, types, schemasDisplayManager);
				}
			}
		}

		private void setSummaryForSchema(MetadataSchemaBuilder schema, MetadataSchemaTypes types,
										 SchemasDisplayManager schemasDisplayManager) {
			SchemaDisplayConfig schemaDisplayConfig = schemasDisplayManager.getSchema(types.getCollection(), schema.getCode());
			List<String> tableMetadatas = schemaDisplayConfig.getTableMetadataCodes();
			List<String> searchResultsMetadatas = schemaDisplayConfig.getSearchResultsMetadataCodes();

			List<String> allSummaryMetadatas = new ArrayList<>();
			allSummaryMetadatas.addAll(tableMetadatas);
			allSummaryMetadatas.addAll(searchResultsMetadatas);

			for (MetadataBuilder metadataBuilder : schema.getMetadatas()) {
				String metadataCode = metadataBuilder.getOriginalMetadata().getCode();
				if (allSummaryMetadatas.contains(metadataCode)
					&& !metadataBuilder.isAvailableInSummary()
					&& metadataBuilder.getInheritance() == null) {
					metadataBuilder.setAvailableInSummary(true);
				}
			}
		}

	}
}
