package com.constellio.app.services.migrations.scripts;

import static java.util.Arrays.asList;

import java.util.Map;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_5_1_7 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.1.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor5_1_7(collection, provider, appLayerFactory).migrate();
		setupDisplayConfig(collection, appLayerFactory, provider);
	}

	private void setupDisplayConfig(String collection, AppLayerFactory appLayerFactory,
			MigrationResourcesProvider migrationResourcesProvider) {

		String configurationTab = "init.facetConfiguration.configuration";
		String valeursTab = "init.facetConfiguration.values";
		String queryTab = "init.facetConfiguration.query";

		Language language = migrationResourcesProvider.getLanguage();
		Map<String, Map<Language, String>> groups = migrationResourcesProvider.getLanguageMapWithKeys(
				asList(configurationTab, valeursTab, queryTab));

		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		SchemaTypeDisplayConfig facetSchemaType = manager.getType(collection, Facet.SCHEMA_TYPE);

		transaction.add(facetSchemaType
				.withMetadataGroup(groups));

		transaction.add(manager.getMetadata(collection, Facet.DEFAULT_SCHEMA, Facet.TITLE)
				.withMetadataGroup(configurationTab));
		transaction.add(manager.getMetadata(collection, Facet.DEFAULT_SCHEMA, Facet.FIELD_DATA_STORE_CODE)
				.withMetadataGroup(configurationTab));
		transaction.add(manager.getMetadata(collection, Facet.DEFAULT_SCHEMA, Facet.ORDER_RESULT)
				.withMetadataGroup(configurationTab));
		transaction.add(manager.getMetadata(collection, Facet.DEFAULT_SCHEMA, Facet.FACET_TYPE)
				.withMetadataGroup(configurationTab));
		transaction.add(manager.getMetadata(collection, Facet.DEFAULT_SCHEMA, Facet.ELEMENT_PER_PAGE)
				.withMetadataGroup(configurationTab));
		transaction.add(manager.getMetadata(collection, Facet.DEFAULT_SCHEMA, Facet.ACTIVE)
				.withMetadataGroup(configurationTab));
		transaction.add(manager.getMetadata(collection, Facet.DEFAULT_SCHEMA, Facet.OPEN_BY_DEFAULT)
				.withMetadataGroup(configurationTab));

		transaction
				.add(manager.getMetadata(collection, Facet.FIELD_SCHEMA, Facet.FIELD_VALUES_LABEL)
						.withMetadataGroup(valeursTab));

		transaction.add(manager.getMetadata(collection, Facet.QUERY_SCHEMA, Facet.LIST_QUERIES)
				.withMetadataGroup(queryTab));

		manager.execute(transaction);
	}

	private class CoreSchemaAlterationFor5_1_7 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor5_1_7(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder facetSchema = typesBuilder.getSchemaType(Facet.SCHEMA_TYPE).getDefaultSchema();
			facetSchema.getMetadata(Facet.TITLE).setDefaultRequirement(true);

			MetadataSchemaBuilder user = typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema();
			user.createUndeletable(User.SIGNATURE).setType(MetadataValueType.TEXT);
		}
	}
}
