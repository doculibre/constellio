package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.appManagement.WrapperConfigurationService;

public class CoreMigrationTo_9_2_42 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.2.42";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		if (FoldersLocator.usingAppWrapper() && collection.equals(Collection.SYSTEM_COLLECTION)) {
			new WrapperConfigurationService().addJavaAdditionnalProperty(new FoldersLocator().getWrapperConf(),
					"java.util.Arrays.useLegacyMergeSort", "true");
		}
	}

}
