package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.CorePermissions.USE_EXTERNAL_APIS_FOR_COLLECTION;
import static java.util.Arrays.asList;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.CopyRetentionRuleFactory;
import com.constellio.app.modules.rm.model.calculators.category.CategoryDefaultCopyRuleInRuleCalculator;
import com.constellio.app.modules.rm.model.calculators.category.CategoryDefaultCopyRuleInRuleCalculator.CategoryDefaultCopyRuleCalculator;
import com.constellio.app.modules.rm.model.calculators.category.CategoryDefaultCopyRuleInRuleCalculator.CategoryDefaultRetentionRuleCalculator;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

public class RMMigrationTo6_5_36 implements MigrationScript {

    @Override
    public String getVersion() {
        return "6.5.36";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
            throws Exception {

        new SchemaAlterationsFor6_5_36(collection, provider, appLayerFactory).migrate();
    }

    public static class SchemaAlterationsFor6_5_36 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationsFor6_5_36(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
            super(collection, provider, factory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            SchemasDisplayManager schemaDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
            SchemaTypesDisplayTransactionBuilder transaction = schemaDisplayManager.newTransactionBuilderFor(collection);

            transaction.in(Event.SCHEMA_TYPE)
                    .addToTable(Event.RECORD_ID)
                    .beforeMetadata(Event.TITLE);

            schemaDisplayManager.execute(transaction.build());
        }
    }

}
