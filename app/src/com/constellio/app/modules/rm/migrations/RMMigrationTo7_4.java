package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.calculators.container.ContainerRecordLocalizationCalculator;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

import static com.constellio.model.entities.Language.French;
import static com.constellio.model.entities.schemas.MetadataTransiency.TRANSIENT_EAGER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;

/**
 * Created by constellios on 2017-07-13.
 */
public class RMMigrationTo7_4 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.4";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new SchemaAlterationsFor7_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    public static class SchemaAlterationsFor7_4 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationsFor7_4(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
            super(collection, provider, factory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder containerRecord = typesBuilder.getSchema(ContainerRecord.DEFAULT_SCHEMA);

            containerRecord.getMetadata(ContainerRecord.DECOMMISSIONING_TYPE).setDefaultRequirement(true);
            containerRecord.getMetadata(ContainerRecord.ADMINISTRATIVE_UNITS).setDefaultRequirement(true);

        }
    }
}
