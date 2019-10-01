package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.RecordCacheType.NOT_CACHED;

public class CoreMigrationTo_9_0_0_4 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.0.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0_0_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_0_4 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_0_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			if (typesBuilder.getCollection().equals(Collection.SYSTEM_COLLECTION)) {
				typesBuilder.getDefaultSchema(UserCredential.SCHEMA_TYPE).get(UserCredential.GLOBAL_GROUPS).setCacheIndex(true);
				typesBuilder.getDefaultSchema(UserCredential.SCHEMA_TYPE).get(UserCredential.USERNAME).setCacheIndex(true);
				typesBuilder.getDefaultSchema(UserCredential.SCHEMA_TYPE).get(UserCredential.STATUS).setCacheIndex(false);
				typesBuilder.getDefaultSchema(UserCredential.SCHEMA_TYPE).get(UserCredential.SERVICE_KEY).setCacheIndex(true);
				typesBuilder.getDefaultSchema(GlobalGroup.SCHEMA_TYPE).get(GlobalGroup.PARENT).setCacheIndex(true);
				typesBuilder.getDefaultSchema(GlobalGroup.SCHEMA_TYPE).get(GlobalGroup.CODE).setCacheIndex(true);
				typesBuilder.getDefaultSchema(GlobalGroup.SCHEMA_TYPE).get(GlobalGroup.STATUS).setCacheIndex(false);
			}

			typesBuilder.getSchemaType(Event.SCHEMA_TYPE).setRecordCacheType(NOT_CACHED);
			typesBuilder.getSchemaType(SearchEvent.SCHEMA_TYPE).setRecordCacheType(NOT_CACHED);
			typesBuilder.getSchemaType(SavedSearch.SCHEMA_TYPE).setRecordCacheType(NOT_CACHED);

			typesBuilder.getDefaultSchema(User.SCHEMA_TYPE).get(User.GROUPS).setCacheIndex(true);
			typesBuilder.getDefaultSchema(User.SCHEMA_TYPE).get(User.USERNAME).setCacheIndex(true);
			typesBuilder.getDefaultSchema(User.SCHEMA_TYPE).get(User.STATUS).setCacheIndex(false);
			typesBuilder.getDefaultSchema(Group.SCHEMA_TYPE).get(Group.PARENT).setCacheIndex(true);
			typesBuilder.getDefaultSchema(Group.SCHEMA_TYPE).get(Group.CODE).setCacheIndex(true);

			typesBuilder.getDefaultSchema(Authorization.SCHEMA_TYPE).get(Authorization.TARGET).setCacheIndex(true);
			typesBuilder.getDefaultSchema(Authorization.SCHEMA_TYPE).get(Authorization.PRINCIPALS).setCacheIndex(true);
		}
	}
}
