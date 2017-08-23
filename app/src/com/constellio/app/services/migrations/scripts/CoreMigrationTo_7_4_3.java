package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_4_3 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.4.3";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new CoreSchemaAlterationFor7_4_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    private class CoreSchemaAlterationFor7_4_3 extends MetadataSchemasAlterationHelper {

        protected CoreSchemaAlterationFor7_4_3(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

        }
    }
}
