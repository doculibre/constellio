package com.constellio.model.services.factories.migration;

import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

public class SecurityMigrationService {
	SystemConfigurationsManager systemConfigurationsManager;
	ModelLayerFactory modelLayerFactory;

	public SecurityMigrationService(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
	}

	public boolean isMigrationRequired() {
		return !(boolean) systemConfigurationsManager.getValue(ConstellioEIMConfigs.IS_ENCRYPTION_MIGRATION_TO_UNIQUE_IV_DONE);
	}

	public void migrateIfRequired() {
		if (this.isMigrationRequired()) {
			new SecurityMigration9_2(modelLayerFactory).migrateAllCollections();
			systemConfigurationsManager.setValue(ConstellioEIMConfigs.IS_ENCRYPTION_MIGRATION_TO_UNIQUE_IV_DONE, true);
		}
	}
}
