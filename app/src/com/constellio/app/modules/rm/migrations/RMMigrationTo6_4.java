package com.constellio.app.modules.rm.migrations;

import java.util.Set;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.validators.FolderValidator;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
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
		SchemaTypesDisplayTransactionBuilder transaction = displayManager.newTransactionBuilderFor(collection);
		transaction.add(displayManager.getSchema(collection, "cart_default"));

		transaction.add(displayManager.getSchema(collection, "userDocument_default")
				.withRemovedDisplayMetadatas("userDocument_default_folder")
				.withRemovedFormMetadatas("userDocument_default_folder"));

		displayManager.execute(transaction.build());

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
			setEnabledNonSystemReservedManuallyValuedMetadataAsDuplicable(typesBuilder);
			typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE).getMetadata(UserDocument.FOLDER).defineReferences().clearSchemas();
			typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE).getMetadata(UserDocument.FOLDER).defineReferencesTo(schemaType(Folder.SCHEMA_TYPE));
		}

		private void updateCartSchema(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder cart = typesBuilder.getSchemaType(Cart.SCHEMA_TYPE).getDefaultSchema();
			if (!cart.hasMetadata(Cart.SHARED_WITH_USERS)) {
				cart.getMetadata(CommonMetadataBuilder.TITLE).defineDataEntry().asManual();
				cart.getMetadata(Cart.OWNER).setUniqueValue(false);

				cart.createUndeletable(Cart.SHARED_WITH_USERS).setMultivalue(true)
						.defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE));
			}
		}

		private void setEnabledNonSystemReservedManuallyValuedMetadataAsDuplicable(
				final MetadataSchemaTypesBuilder typesBuilder) {
			final Set<MetadataBuilder> metadataBuilders = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getAllMetadatas();
			metadataBuilders.addAll(typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getAllMetadatas());
			for (final MetadataBuilder metadataBuilder : metadataBuilders) {
				if ((metadataBuilder != null) && metadataBuilder.getEnabled() && !metadataBuilder.isSystemReserved() && (
						(metadataBuilder.getDataEntry() != null) && DataEntryType.MANUAL
								.equals(metadataBuilder.getDataEntry().getType()))) {
					metadataBuilder.setDuplicable(true);
				}
			}
		}

		private void updateFolderSchema(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folder = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
			folder.getMetadata(Folder.CATEGORY_ENTERED).setTaxonomyRelationship(false);
			folder.getMetadata(Folder.CATEGORY).setTaxonomyRelationship(true);

			folder.getMetadata(Folder.ADMINISTRATIVE_UNIT_ENTERED).setTaxonomyRelationship(false);
			folder.getMetadata(Folder.ADMINISTRATIVE_UNIT).setTaxonomyRelationship(true);
		}
	}
}
