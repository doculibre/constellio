package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.document.DocumentMimeTypeCalculator;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo7_0_5 implements MigrationScript {

    AppLayerFactory appLayerFactory;
    String collection;
    MigrationResourcesProvider migrationResourcesProvider;

    @Override
    public String getVersion() {
        return "7.0.5";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
            throws Exception {
        this.appLayerFactory = factory;
        this.collection = collection;
        migrationResourcesProvider = provider;
        new SchemaAlterationsFor7_0_5(collection, provider, factory).migrate();
    }

    public static class SchemaAlterationsFor7_0_5 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationsFor7_0_5(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
            super(collection, provider, factory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE).create(Document.MIME_TYPE).setType(MetadataValueType.STRING)
                    .defineDataEntry().asCalculated(DocumentMimeTypeCalculator.class);
        }
    }
}