package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.UserDocumentContentSizeCalculator;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_8_2_0_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.2.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		new CoreMigrationTo_8_2_0_1.SchemaAlterationFor8_2_0_1(collection, migrationResourcesProvider, appLayerFactory)
				.migrate();
	}

	class SchemaAlterationFor8_2_0_1 extends MetadataSchemasAlterationHelper {

		private SchemaAlterationFor8_2_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder userDocumentSchema = typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE);

			if (!userDocumentSchema.hasMetadata(UserDocument.CONTENT_SIZE)) {
				MetadataBuilder userDocumentContentSize = userDocumentSchema.createUndeletable(UserDocument.CONTENT_SIZE)
						.setType(MetadataValueType.NUMBER).defineDataEntry()
						.asCalculated(UserDocumentContentSizeCalculator.class);

				MetadataSchemaBuilder userSchema = typesBuilder.getDefaultSchema(User.SCHEMA_TYPE);
				userSchema.createUndeletable(User.USER_DOCUMENT_SIZE_SUM)
						.setType(MetadataValueType.NUMBER).setEssential(false).defineDataEntry()
						.asSum(userDocumentSchema.getMetadata(UserDocument.USER), userDocumentContentSize);
			}
		}
	}
}
