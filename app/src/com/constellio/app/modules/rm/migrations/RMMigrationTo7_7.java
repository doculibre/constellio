package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMMigrationTo7_7 extends MigrationHelper implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.7";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new RMMigrationTo7_7.SchemaAlterationFor7_7(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    class SchemaAlterationFor7_7 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_7(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                         AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        public String getVersion() {
            return "7.7";
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getDefaultSchema(Cart.SCHEMA_TYPE).get(Schemas.TITLE).setDefaultRequirement(true);
        }
    }
}
