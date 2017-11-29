package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESMigrationTo7_6_6 extends MigrationHelper implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.6.6";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
            throws Exception {
        new ESMigrationTo7_6_6.SchemaAlterationFor7_6_6(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class SchemaAlterationFor7_6_6 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_6_6(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                             AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getDefaultSchema(ConnectorSmbDocument.SCHEMA_TYPE).getMetadata(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode()).setEssentialInSummary(true);
        }
    }
}
