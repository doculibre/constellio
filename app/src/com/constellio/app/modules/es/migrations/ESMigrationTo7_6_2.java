package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESMigrationTo7_6_2 extends MigrationHelper implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.6.2";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new SchemaAlterationFor7_6_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    static class SchemaAlterationFor7_6_2 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_6_2(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder documentDefaultSchema = typesBuilder.getSchemaType(ConnectorHttpDocument.SCHEMA_TYPE).getDefaultSchema();
            documentDefaultSchema.get(ConnectorHttpDocument.CHARSET).addLabel(Language.French, "Jeu de caractères");
            documentDefaultSchema.get(ConnectorHttpDocument.DIGEST).addLabel(Language.French, "Résumé");
        }
    }
}
