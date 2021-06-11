package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMMessage;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Conversation;
import com.constellio.model.entities.records.wrappers.Message;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class RMMigrationTo9_3_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.3.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_3_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_3_2 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_3_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
								 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder messageSchemaType = typesBuilder.getSchemaType(Message.SCHEMA_TYPE);
			MetadataSchemaBuilder messageDefaultSchema = messageSchemaType.getDefaultSchema();
			MetadataBuilder linkedDocumentsDataBuilder = messageDefaultSchema.createUndeletable(RMMessage.LINKED_DOCUMENTS).setType(REFERENCE)
					.defineReferencesTo(typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE)).setMultivalue(true);
			messageDefaultSchema.createUndeletable(RMMessage.LINKED_DOCUMENTS_TITLES).setSearchable(true).setMultivalue(true).setType(STRING)
					.defineDataEntry().asCopied(linkedDocumentsDataBuilder, typesBuilder.getSchema(Document.DEFAULT_SCHEMA).get(Schemas.TITLE_CODE));
			MetadataSchemaBuilder folderDefaultSchema = typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE);
			folderDefaultSchema.createUndeletable(Folder.CONVERSATION).setType(REFERENCE)
					.defineReferencesTo(typesBuilder.getDefaultSchema(Conversation.SCHEMA_TYPE));
		}
	}
}
