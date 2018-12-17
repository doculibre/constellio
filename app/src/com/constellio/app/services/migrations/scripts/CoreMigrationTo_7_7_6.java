package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.io.File;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

public class CoreMigrationTo_7_7_6 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.7.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_7_6(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_7_7_6 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_7_6(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			if (Collection.SYSTEM_COLLECTION.equals(collection)) {
				File currentWrapper = appLayerFactory.getModelLayerFactory().getFoldersLocator().getWrapperConf();
				//				WrapperConfUpdateUtils.setAdditionalSettings(currentWrapper,
				//						appLayerFactory.getModelLayerFactory().getIOServicesFactory().newFileService());
			}

			MetadataSchemaBuilder searchEvent = typesBuilder.getSchemaType(SearchEvent.SCHEMA_TYPE).getDefaultSchema();
			if (!searchEvent.hasMetadata(SearchEvent.CAPSULE)) {
				searchEvent.create(SearchEvent.CAPSULE).setType(REFERENCE).setMultivalue(true)
						.defineReferencesTo(typesBuilder.getSchemaType(Capsule.SCHEMA_TYPE));
			}
		}
	}
}