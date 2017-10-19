package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESMigrationTo7_6_1_1 extends MigrationHelper implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.6.1.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new SchemaAlterationFor7_6_1_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
        SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
        metadataSchemasDisplayManager.saveMetadata(metadataSchemasDisplayManager.getMetadata(collection, ConnectorSmbDocument.DEFAULT_SCHEMA + "_" + ConnectorSmbDocument.CONNECTOR_URL)
                .withVisibleInAdvancedSearchStatus(true));
        metadataSchemasDisplayManager.saveMetadata(metadataSchemasDisplayManager.getMetadata(collection, ConnectorSmbFolder.DEFAULT_SCHEMA + "_" + ConnectorSmbFolder.CONNECTOR_URL)
                .withVisibleInAdvancedSearchStatus(true));
    }

    static class SchemaAlterationFor7_6_1_1 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_6_1_1(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder documentDefaultSchema = typesBuilder.getSchemaType(ConnectorSmbDocument.SCHEMA_TYPE).getDefaultSchema();
            documentDefaultSchema.get(ConnectorSmbDocument.PARENT_CONNECTOR_URL).addLabel(Language.French, "Chemin réseau parent");
            documentDefaultSchema.get(ConnectorSmbDocument.CONNECTOR_URL).addLabel(Language.French, "Chemin réseau").setSearchable(false);

            MetadataSchemaBuilder folderDefaultSchema = typesBuilder.getSchemaType(ConnectorSmbFolder.SCHEMA_TYPE).getDefaultSchema();
            folderDefaultSchema.get(ConnectorSmbFolder.PARENT_CONNECTOR_URL).addLabel(Language.French, "Chemin réseau parent");
            folderDefaultSchema.get(ConnectorSmbFolder.CONNECTOR_URL).addLabel(Language.French, "Chemin réseau").setSearchable(false).setSystemReserved(false);
        }
    }
}
