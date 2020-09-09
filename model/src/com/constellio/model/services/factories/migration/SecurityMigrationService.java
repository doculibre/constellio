package com.constellio.model.services.factories.migration;

import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManagerRuntimeException.MetadataSchemasManagerRuntimeException_NoSuchCollection;

public class SecurityMigrationService {
	SystemConfigurationsManager systemConfigurationsManager;
	ModelLayerFactory modelLayerFactory;

	public SecurityMigrationService(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
	}

	public boolean isMigrationRequired() {
		try {
			modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(Collection.SYSTEM_COLLECTION).getDefaultSchema(UserCredential.SCHEMA_TYPE).getMetadata(UserCredential.SYNC_MODE);
			return false;
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			return true;
		} catch (MetadataSchemasManagerRuntimeException_NoSuchCollection e) {
			return false;
		}
	}

	public void migrateIfRequired() {
		if (this.isMigrationRequired()) {
			new SecurityMigration9_2(modelLayerFactory).migrateAllCollections();
		}
	}
}
