package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo5_1_7 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.1.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		new SchemaAlterationsFor5_1_7(collection, provider, factory).migrate();
	}

	public static class SchemaAlterationsFor5_1_7 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor5_1_7(String collection, MigrationResourcesProvider provider,
											AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			createCartSchemaType(builder);
		}

		private void createCartSchemaType(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaTypeBuilder schemaType = builder.createNewSchemaTypeWithSecurity(Cart.SCHEMA_TYPE).setSecurity(false);
			MetadataSchemaBuilder schema = schemaType.getDefaultSchema();

			schema.createUndeletable(Cart.OWNER).setDefaultRequirement(true).setUniqueValue(true)
					.defineReferencesTo(builder.getSchemaType(User.SCHEMA_TYPE));

			schema.getMetadata(CommonMetadataBuilder.TITLE).defineDataEntry().asCopied(schema.getMetadata(Cart.OWNER),
					builder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema().getMetadata(CommonMetadataBuilder.TITLE));
		}
	}
}
