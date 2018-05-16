package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.Iterator;

public class CoreMigrationTo_7_7_5 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.7.5";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        SystemConfigurationsManager configManager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();
        if(Collection.SYSTEM_COLLECTION.equals(collection) && configManager.getValue(ConstellioEIMConfigs.AUTHENTIFICATION_IMAGE) == null) {
            configManager.setValue(ConstellioEIMConfigs.AUTHENTIFICATION_IMAGE, configManager.getValue(ConstellioEIMConfigs.LOGO));
        }
    }
}
