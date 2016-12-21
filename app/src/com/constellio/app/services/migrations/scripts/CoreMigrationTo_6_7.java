package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.type.SolrAuthorizationDetails;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.security.XMLAuthorizationDetails;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.AuthorizationDetailsManager;

import java.util.Iterator;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.*;

public class CoreMigrationTo_6_7 implements MigrationScript {

    @Override
    public String getVersion() {
        return "6.7";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
            throws Exception {
        new CoreSchemaAlterationFor6_7(collection, migrationResourcesProvider, appLayerFactory).migrate();
        convertXMLAuthorizationDetailsToSolrAuthorizationDetails(collection, appLayerFactory);
    }

    private void convertXMLAuthorizationDetailsToSolrAuthorizationDetails(String collection, AppLayerFactory appLayerFactory) {
        AuthorizationDetailsManager manager = appLayerFactory.getModelLayerFactory().getAuthorizationDetailsManager();
        Map<String, XMLAuthorizationDetails> xmlAuthorizationDetailsList = manager.getAuthorizationsDetails(collection);
        Iterator iterator = xmlAuthorizationDetailsList.entrySet().iterator();
        while(iterator.hasNext())  {
            Map.Entry pair = (Map.Entry) iterator.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }

    private class CoreSchemaAlterationFor6_7 extends MetadataSchemasAlterationHelper {
        public CoreSchemaAlterationFor6_7(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                            AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            createReportSchemaType(typesBuilder);
        }

        private MetadataSchemaTypeBuilder createReportSchemaType(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaTypeBuilder type = typesBuilder.createNewSchemaType(SolrAuthorizationDetails.SCHEMA_TYPE);
            MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();

            defaultSchema.createUndeletable(SolrAuthorizationDetails.ROLES).setType(STRING).setMultivalue(true);
            defaultSchema.createUndeletable(SolrAuthorizationDetails.SYNCED).setType(BOOLEAN);
            defaultSchema.createUndeletable(SolrAuthorizationDetails.START_DATE).setType(DATE);
            defaultSchema.createUndeletable(SolrAuthorizationDetails.END_DATE).setType(DATE);

            return type;
        }
    }
}
