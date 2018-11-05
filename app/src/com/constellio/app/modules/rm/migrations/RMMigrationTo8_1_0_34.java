package com.constellio.app.modules.rm.migrations;

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

public class RMMigrationTo8_1_0_34 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.1.0.34";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		new RMMigrationTo8_1_0_34.SchemaAlterationFor8_1_0_34(collection, migrationResourcesProvider, appLayerFactory)
				.migrate();
	}

	class SchemaAlterationFor8_1_0_34 extends MetadataSchemasAlterationHelper {

		private SchemaAlterationFor8_1_0_34(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder userDocumentSchema = typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE);
			MetadataBuilder userDocumentContentSize = userDocumentSchema.createUndeletable(UserDocument.CONTENT_SIZE)
					.setType(MetadataValueType.NUMBER).defineDataEntry()
					.asCalculated(UserDocumentContentSizeCalculator.class);

			typesBuilder.getDefaultSchema(User.SCHEMA_TYPE).createUndeletable(User.USER_DOCUMENT_SIZE_SUM)
					.setType(MetadataValueType.NUMBER).setEssential(false).defineDataEntry()
					.asSum(userDocumentSchema.getMetadata(UserDocument.USER), userDocumentContentSize);
		}
	}
}
