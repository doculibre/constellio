package com.constellio.app.services.migrations.scripts;

import java.io.File;

import com.constellio.app.api.admin.services.TLSConfigUtils;
import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_7_6 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.7.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_7_6(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_7_7_6 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_7_6(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			if (Collection.SYSTEM_COLLECTION.equals(collection)) {
				File currentWrapper = appLayerFactory.getModelLayerFactory().getFoldersLocator().getWrapperConf();
				TLSConfigUtils.setAdditionalSettings(currentWrapper,
						appLayerFactory.getModelLayerFactory().getIOServicesFactory().newFileService());
			}
		}
	}
}
