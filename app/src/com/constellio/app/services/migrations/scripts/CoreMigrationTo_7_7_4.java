package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.Iterator;
import java.util.Set;

public class CoreMigrationTo_7_7_4 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.7.4";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new CoreSchemaAlterationFor_7_7_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class CoreSchemaAlterationFor_7_7_4 extends MetadataSchemasAlterationHelper {

        protected CoreSchemaAlterationFor_7_7_4(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            Iterator<MetadataBuilder> metadataIterator = typesBuilder.getAllMetadatas().iterator();
            while (metadataIterator.hasNext()) {
                MetadataBuilder metadata = metadataIterator.next();
                if(metadata.getType() == MetadataValueType.REFERENCE) {
                    metadata.setSearchable(false);
                    metadata.setSchemaAutocomplete(false);
                }
            }
        }
    }
}
