package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.SolrUserCredential;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

/**
 * Created by Marco on 2017-03-14.
 */
public class CoreMigrationTo_7_1_2 extends MigrationHelper implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.1.2";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new SchemaAlterationFor7_1_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
    }

    private class SchemaAlterationFor7_1_2 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_1_2(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaBuilder builder = typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema();
            builder.create(User.FAX).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();
            builder.create(User.ADDRESS).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();

            if (typesBuilder.getCollection().equals(Collection.SYSTEM_COLLECTION)) {
                MetadataSchemaBuilder UserCredentialBuilder = typesBuilder.getSchemaType(SolrUserCredential.SCHEMA_TYPE).getDefaultSchema();
                UserCredentialBuilder.create(SolrUserCredential.ADDRESS).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();
                UserCredentialBuilder.create(SolrUserCredential.FAX).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();
                UserCredentialBuilder.create(SolrUserCredential.JOB_TITLE).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();
                UserCredentialBuilder.create(SolrUserCredential.PHONE).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();
            }
        }
    }
}
