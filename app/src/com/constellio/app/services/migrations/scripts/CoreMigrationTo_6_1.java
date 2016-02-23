package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.WorkflowTask;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_6_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		new CoreSchemaAlterationFor6_1(collection, provider, appLayerFactory).migrate();
	}

	private class CoreSchemaAlterationFor6_1 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor6_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder collectionSchemaType = typesBuilder.getSchemaType(Collection.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder eventSchemaType = typesBuilder.getSchemaType(Event.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder userSchemaType = typesBuilder.getSchemaType(User.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder groupSchemaType = typesBuilder.getSchemaType(Group.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder workflowTaskSchemaType = typesBuilder.getSchemaType(WorkflowTask.SCHEMA_TYPE);

			collectionSchemaType.setSecurity(false);
			eventSchemaType.setSecurity(false);
			userSchemaType.setSecurity(false);
			groupSchemaType.setSecurity(false);
			workflowTaskSchemaType.setSecurity(false);
		}
	}
}
