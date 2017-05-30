package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

/**
 * Created by constellios on 2017-05-02.
 */
public class RMMigrationTo7_3 implements MigrationScript {

	@Override
	public String getVersion() {
		return "7.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor7_3(collection, migrationResourcesProvider, appLayerFactory).migrate();

		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		manager.saveSchema(manager.getSchema(collection, Category.DEFAULT_SCHEMA)
				.withNewFormAndDisplayMetadatas(Category.DEFAULT_SCHEMA + "_" + Category.DEACTIVATE));

		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);

		transaction.in(ContainerRecord.SCHEMA_TYPE).addToForm(ContainerRecord.ADMINISTRATIVE_UNITS)
				.beforeMetadata(ContainerRecord.ADMINISTRATIVE_UNIT);
		transaction.in(ContainerRecord.SCHEMA_TYPE).addToDisplay(ContainerRecord.ADMINISTRATIVE_UNITS)
				.beforeMetadata(ContainerRecord.ADMINISTRATIVE_UNIT);

		transaction.in(ContainerRecord.SCHEMA_TYPE).removeFromForm(ContainerRecord.ADMINISTRATIVE_UNIT);
		transaction.in(ContainerRecord.SCHEMA_TYPE).removeFromDisplay(ContainerRecord.ADMINISTRATIVE_UNIT);

		transaction.addReplacing(manager.getMetadata(collection, ContainerRecord.DEFAULT_SCHEMA + "_" +
				ContainerRecord.ADMINISTRATIVE_UNITS).withVisibleInAdvancedSearchStatus(true));
		manager.execute(transaction.build());

	}

	class SchemaAlterationFor7_3 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "7.3";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			//typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.COPY_STATUS_ENTERED).setDefaultValue(CopyType.PRINCIPAL);
			typesBuilder.getDefaultSchema(Category.SCHEMA_TYPE).create(Category.DEACTIVATE).setType(MetadataValueType.BOOLEAN)
					.setDefaultValue(null);

			MetadataSchemaBuilder schemaBuilder = typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE);
			boolean required = Boolean.TRUE == schemaBuilder.get(ContainerRecord.ADMINISTRATIVE_UNIT).getDefaultRequirement();
			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.ADMINISTRATIVE_UNIT)
					.setTaxonomyRelationship(false).setDefaultRequirement(false).setEnabled(false).setEssential(false);
			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.ADMINISTRATIVE_UNITS)
					.setTaxonomyRelationship(true).setDefaultRequirement(required).setEssential(true);
		}
	}

}
