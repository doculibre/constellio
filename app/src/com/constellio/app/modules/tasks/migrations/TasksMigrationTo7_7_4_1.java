package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class TasksMigrationTo7_7_4_1 extends MigrationHelper implements MigrationScript {

	public static final String USER_TASK_DEFAULT_READ_BY_USER = Task.DEFAULT_SCHEMA + "_" + Task.READ_BY_USER;
	public static final String LUE = "Lue";
	public static final String READ = "Read";
	public static final String INIT_USER_TASK_TAB = "default:init.userTask.definition";
	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "7.7.4.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		new SchemaAlterationFor7_7_4_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		manager.saveSchema(manager.getSchema(collection, Task.DEFAULT_SCHEMA).withNewFormAndDisplayMetadatas(
				USER_TASK_DEFAULT_READ_BY_USER
		));

		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();

		transaction.add(manager.getMetadata(collection, USER_TASK_DEFAULT_READ_BY_USER)
							   .withMetadataGroup(INIT_USER_TASK_TAB).withInputType(MetadataInputType.CHECKBOXES));

		manager.execute(transaction);
	}

	class SchemaAlterationFor7_7_4_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_7_4_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder schema = typesBuilder.getSchema(Task.DEFAULT_SCHEMA);
			if (!schema.hasMetadata(Task.READ_BY_USER)) {
				schema.createUndeletable(Task.READ_BY_USER).setType(MetadataValueType.BOOLEAN).setDefaultValue(Boolean.FALSE)
					  .addLabel(Language.French, LUE).addLabel(Language.English, READ);
			}
		}
	}
}
