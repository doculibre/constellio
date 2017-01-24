package com.constellio.app.modules.complementary.esRmRobots.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderDirectlyInThePlanActionParameters.*;

public class ESRMRobotsMigrationTo7_1 implements MigrationScript {

    @Override
    public String getVersion() {
        return "7.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
            throws Exception {

        new SchemaAlterationFor7_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

        configureClassifyInTaxonomyParametersForm(collection, migrationResourcesProvider, appLayerFactory);
    }

    class SchemaAlterationFor7_1 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                         AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        public String getVersion() {
            return "7.1";
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            setupClassifyConnectorFolderDirectlyInThePlanActionParametersSchema();
        }

        private void setupClassifyConnectorFolderDirectlyInThePlanActionParametersSchema() {

            MetadataSchemaTypeBuilder subdivisionSchemaType = typesBuilder.getSchemaType(UniformSubdivision.SCHEMA_TYPE);

            MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
                    .getCustomSchema(SCHEMA_LOCAL_CODE);

            schema.create(DEFAULT_UNIFORM_SUBDIVISION)
                    .setDefaultRequirement(false).defineReferencesTo(subdivisionSchemaType);
        }
    }

    private void configureClassifyInTaxonomyParametersForm(String collection,
                                                           MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {

        String defaultValuesTab = "tab.defaultValues";

        String inPlanSchema = SCHEMA;

        SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
        SchemaTypesDisplayTransactionBuilder transaction = schemasDisplayManager.newTransactionBuilderFor(collection);

        transaction.add(schemasDisplayManager.getSchema(collection, inPlanSchema).withNewFormMetadataBefore(inPlanSchema + "_" + DEFAULT_UNIFORM_SUBDIVISION, inPlanSchema + "_" + DEFAULT_RETENTION_RULE));

        transaction.add(schemasDisplayManager.getMetadata(collection, inPlanSchema, DEFAULT_UNIFORM_SUBDIVISION)
                .withMetadataGroup(defaultValuesTab));

        schemasDisplayManager.execute(transaction.build());
    }
}
