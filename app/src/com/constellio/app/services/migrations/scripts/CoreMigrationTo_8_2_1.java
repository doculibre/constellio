package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;

public class CoreMigrationTo_8_2_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.2.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory)
			throws Exception {
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		new CoreSchemaAlterationFor_8_2_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_8_2_1 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_2_1(String collection,
				MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getDefaultSchema(User.SCHEMA_TYPE).createUndeletable(User.TAXONOMY_DISPLAY_ORDER)
					.setType(MetadataValueType.STRING).setMultivalue(true);

			typesBuilder.getDefaultSchema(User.SCHEMA_TYPE).createUndeletable(User.DO_NOT_RECEIVE_EMAILS)
					.setType(BOOLEAN);

			if (Collection.SYSTEM_COLLECTION.equals(typesBuilder.getCollection())) {
				typesBuilder.getDefaultSchema(UserCredential.SCHEMA_TYPE).createUndeletable(UserCredential.DO_NOT_RECEIVE_EMAILS)
						.setType(BOOLEAN);
			}

			Set<MetadataBuilder> booleanMetadatas = typesBuilder.getAllMetadatasOfType(BOOLEAN);
			Set<MetadataBuilder> numberMetadatas = typesBuilder.getAllMetadatasOfType(NUMBER);
			Set<MetadataBuilder> integerMetadatas = typesBuilder.getAllMetadatasOfType(INTEGER);

			for (MetadataBuilder metadata : booleanMetadatas) {
				if (metadata.getInheritance() == null) {
					metadata.setSearchable(false).setSchemaAutocomplete(false);
				}
			}

			for (MetadataBuilder metadata : numberMetadatas) {
				if (metadata.getInheritance() == null) {
					metadata.setSearchable(false).setSchemaAutocomplete(false);
				}
			}

			for (MetadataBuilder metadata : integerMetadatas) {
				if (metadata.getInheritance() == null) {
					metadata.setSearchable(false).setSchemaAutocomplete(false);
				}
			}
		}
	}
}
