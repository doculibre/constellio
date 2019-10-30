package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CoreMigrationTo_9_0_1_3 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.1.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new CoreMigrationTo_9_0_1_3.SchemaAlterationFor9_0_1_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_1_3 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_1_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
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
					boolean multilingual = defaultSchemaBuilder.getMetadata(Schemas.TITLE_CODE).isMultiLingual();

					if (!defaultSchemaBuilder.hasMetadata("abbreviation")) {
						MetadataBuilder abbreviationMetadata = defaultSchemaBuilder.createUndeletable(ValueListItem.ABBREVIATION)
								.setType(MetadataValueType.STRING).setSearchable(true).setMultiLingual(multilingual);

						for (Language language : typesBuilder.getLanguages()) {
							abbreviationMetadata.addLabel(language, $("init.valuelist.default.abbreviation"));
						}
					}
				});
			});
		}
	}
}
