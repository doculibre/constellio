package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo7_7_5_4 extends MigrationHelper implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.7.5.4";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new SchemaAlterationFor7_7_5_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class SchemaAlterationFor7_7_5_4 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_7_5_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                           AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder sourceDefaultSchema = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
            MetadataSchemaBuilder destinationDefaultSchema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();

            MetadataBuilder refMetadata = destinationDefaultSchema.getMetadata(Document.FOLDER);
            MetadataBuilder sourceMetadata = sourceDefaultSchema.getMetadata(Folder.CATEGORY_CODE);
            MetadataBuilder destinationMetadata = destinationDefaultSchema.createUndeletable(Document.FOLDER_CATEGORY_CODE);
            destinationMetadata.setType(sourceMetadata.getType());
            destinationMetadata.defineDataEntry().asCopied(refMetadata, sourceMetadata);
        }
    }
}
