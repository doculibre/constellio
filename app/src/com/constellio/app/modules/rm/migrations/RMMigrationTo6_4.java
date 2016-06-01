package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.validators.FolderValidator;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo6_4 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		new SchemaAlterationsFor6_4(collection, provider, factory).migrate();

		SchemasDisplayManager displayManager = factory.getMetadataSchemasDisplayManager();
		displayManager.saveSchema(displayManager.getSchema(collection, "cart_default"));

	}

	public static class SchemaAlterationsFor6_4 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor6_4(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).defineValidators().add(FolderValidator.class);
			updateCartSchema(typesBuilder);
			updateFolderSchema(typesBuilder);
		}

		private void updateCartSchema(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder cart = typesBuilder.getSchemaType(Cart.SCHEMA_TYPE).getDefaultSchema();
			cart.getMetadata(CommonMetadataBuilder.TITLE).defineDataEntry().asManual();
			cart.getMetadata(Cart.OWNER).setUniqueValue(false);
			cart.createUndeletable(Cart.SHARED_WITH_USERS).setMultivalue(true)
					.defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE));
		}

		private void updateFolderSchema(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folder = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
			//			folder.getMetadata(Folder.CATEGORY_ENTERED).setTaxonomyRelationship(false);
			//			folder.getMetadata(Folder.CATEGORY).setTaxonomyRelationship(true);
			//
			//			folder.getMetadata(Folder.ADMINISTRATIVE_UNIT_ENTERED).setTaxonomyRelationship(false);
			//			folder.getMetadata(Folder.ADMINISTRATIVE_UNIT).setTaxonomyRelationship(true);
		}
	}
}
