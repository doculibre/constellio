package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_7 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.7";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
            throws Exception {
        SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
        SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
        transaction.add(displayManager.getSchema(collection, User.DEFAULT_SCHEMA).withNewDisplayMetadataQueued(User.DEFAULT_PAGE_LENGTH));
    }
}
