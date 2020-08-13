package com.constellio.app.services.migrations.scripts;

import com.constellio.app.api.admin.services.WrapperConfUpdateUtils;
import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.conf.FoldersLocatorMode;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.io.File;

public class CoreMigrationTo_9_0_3_22 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.0.3.22";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		if (collection.equals(Collection.SYSTEM_COLLECTION)) {
			new CoreSchemaAlterationFor_9_0_3_22(collection, migrationResourcesProvider, appLayerFactory).migrate();
		} else {
			FoldersLocator foldersLocator = appLayerFactory.getModelLayerFactory().getFoldersLocator();
			if (foldersLocator.getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
				File currentWrapper = foldersLocator.getWrapperConf();
				WrapperConfUpdateUtils.setSettingAdditionalEphemeralDHKeySize(currentWrapper,
						appLayerFactory.getModelLayerFactory().getIOServicesFactory().newFileService());
			}
		}
	}

	class CoreSchemaAlterationFor_9_0_3_22 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_9_0_3_22(String collection,
												   MigrationResourcesProvider migrationResourcesProvider,
												   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
		}
	}
}
