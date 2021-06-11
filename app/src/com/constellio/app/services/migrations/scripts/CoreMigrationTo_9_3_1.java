package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.calculators.MessageHasUrlInBodyCalculator;
import com.constellio.model.entities.records.wrappers.Conversation;
import com.constellio.model.entities.records.wrappers.Message;
import com.constellio.model.entities.records.wrappers.MessageBodyType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;

public class CoreMigrationTo_9_3_1 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.3.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) {
		new CoreSchemaAlterationFor_9_3_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_9_3_1 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor_9_3_1(String collection,
											 MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.createNewSchemaTypeWithSecurity(Conversation.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder messageSchemaType = typesBuilder.createNewSchemaTypeWithSecurity(Message.SCHEMA_TYPE);
			MetadataSchemaBuilder messageDefaultSchema = messageSchemaType.getDefaultSchema();
			messageDefaultSchema.createUndeletable(Message.CONVERSATION).setType(REFERENCE)
					.defineReferencesTo(typesBuilder.getDefaultSchema(Conversation.SCHEMA_TYPE)).setDefaultRequirement(true);
			messageDefaultSchema.createUndeletable(Message.MESSAGE_AUTHOR).setType(REFERENCE)
					.defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE)).setDefaultRequirement(true);
			messageDefaultSchema.createUndeletable(Message.MESSAGE_BODY).setType(TEXT).setDefaultRequirement(true).setSearchable(true);
			messageDefaultSchema.createUndeletable(Message.MESSAGE_BODY_TYPE).setType(ENUM).defineAsEnum(MessageBodyType.class);
			MetadataBuilder parentMetadata = messageDefaultSchema.createUndeletable(Message.MESSAGE_PARENT).setType(REFERENCE)
					.defineReferencesTo(typesBuilder.getDefaultSchema(Message.SCHEMA_TYPE));
			messageDefaultSchema.createUndeletable(Message.MESSAGE_REPLY_COUNT).setType(NUMBER)
					.defineDataEntry().asReferenceCount(parentMetadata);
			messageDefaultSchema.createUndeletable(Message.HAS_URL_IN_MESSAGE).setType(BOOLEAN)
					.defineDataEntry().asCalculated(MessageHasUrlInBodyCalculator.class);
		}
	}
}
