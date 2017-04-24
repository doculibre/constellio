package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.RMCollection;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_2 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.2";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
                        AppLayerFactory appLayerFactory) {
        new CoreSchemaAlterationFor7_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    private class CoreSchemaAlterationFor7_2 extends MetadataSchemasAlterationHelper {
        public CoreSchemaAlterationFor7_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                          AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getDefaultSchema(RMCollection.SCHEMA_TYPE).createUndeletable(RMCollection.ORGANIZATION_NUMBER).setType(MetadataValueType.STRING).setSystemReserved(true);
            typesBuilder.getDefaultSchema(RMCollection.SCHEMA_TYPE).createUndeletable(RMCollection.CONSERVATION_CALENDAR_NUMBER).setType(MetadataValueType.STRING).setSystemReserved(true);
        }
    }
}
