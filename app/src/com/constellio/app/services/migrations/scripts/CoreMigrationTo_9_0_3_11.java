package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.metadatas.IllegalCharactersValidator;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

import java.util.ArrayList;
import java.util.List;

public class CoreMigrationTo_9_0_3_11 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.3.11";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new CoreMigrationTo_9_0_3_11.SchemaAlterationFor9_0_3_11(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_3_11 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_3_11(String collection, MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			TaxonomiesManager taxonomiesManager = appLayerFactory.getModelLayerFactory().getTaxonomiesManager();

			List<Taxonomy> taxonomies = new ArrayList<>(taxonomiesManager.getEnabledTaxonomies(collection));
			taxonomies.addAll(taxonomiesManager.getDisabledTaxonomies(collection));

			taxonomies.forEach(taxonomy -> {
				taxonomy.getSchemaTypes().forEach(schemaType -> {
					MetadataSchemaBuilder defaultSchemaBuilder = typesBuilder.getDefaultSchema(schemaType);

					defaultSchemaBuilder.get(ValueListItem.CODE).addValidator(IllegalCharactersValidator.class);
					defaultSchemaBuilder.get(ValueListItem.TITLE).addValidator(IllegalCharactersValidator.class);

					if (defaultSchemaBuilder.hasMetadata(ValueListItem.ABBREVIATION)) {
						defaultSchemaBuilder.get(ValueListItem.ABBREVIATION)
								.addValidator(IllegalCharactersValidator.class);
					}
				});
			});
		}
	}
}
