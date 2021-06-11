package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.AgentStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;

/**
 * Created by Nicolas D'Amours on 2017-
 */
public class CoreMigrationTo_7_0_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreAlternationFor7_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreAlternationFor7_0_1 extends MetadataSchemasAlterationHelper {

		public CoreAlternationFor7_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
									   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			if (!typesBuilder.getSchema(User.DEFAULT_SCHEMA).hasMetadata(User.AGENT_ENABLED)) {
				typesBuilder.getSchema(User.DEFAULT_SCHEMA).create(User.AGENT_ENABLED).setType(MetadataValueType.BOOLEAN)
						.setDefaultValue(true);

				MetadataSchemaTypeBuilder type = typesBuilder.createNewSchemaTypeWithSecurity(UserFolder.SCHEMA_TYPE);
				MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();
				type.setSecurity(false);
				defaultSchema.create(UserFolder.USER).setType(MetadataValueType.REFERENCE).setEssential(true)
						.defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE));
				defaultSchema.create(UserFolder.FORM_CREATED_ON).setType(MetadataValueType.DATE_TIME).setEssential(false);
				defaultSchema.create(UserFolder.FORM_MODIFIED_ON).setType(MetadataValueType.DATE_TIME).setEssential(false);
				defaultSchema.create(UserFolder.PARENT_USER_FOLDER).setType(MetadataValueType.REFERENCE).setEssential(false)
						.defineReferencesTo(typesBuilder.getSchemaType(UserFolder.SCHEMA_TYPE));

				typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE).create(UserDocument.USER_FOLDER).setEssential(false)
						.setType(MetadataValueType.REFERENCE)
						.defineReferencesTo(typesBuilder.getSchemaType(UserFolder.SCHEMA_TYPE));
				typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE).create(UserDocument.FORM_CREATED_ON).setEssential(false)
						.setType(MetadataValueType.DATE_TIME);
				typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE).create(UserDocument.FORM_MODIFIED_ON).setEssential(false)
						.setType(MetadataValueType.DATE_TIME);

				if (Collection.SYSTEM_COLLECTION.equals(typesBuilder.getCollection())) {
					MetadataSchemaBuilder userCredentialSchema = typesBuilder.getSchema(UserCredential.DEFAULT_SCHEMA);
					userCredentialSchema.create(UserCredential.AGENT_STATUS).setType(ENUM).defineAsEnum(AgentStatus.class);
				}
			}
		}
	}
}
