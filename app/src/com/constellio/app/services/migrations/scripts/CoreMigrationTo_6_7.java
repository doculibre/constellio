package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.security.XMLAuthorizationDetails;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
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

    private void convertXMLAuthorizationDetailsToSolrAuthorizationDetails(String collection, AppLayerFactory appLayerFactory)
            throws RecordServicesException {

        AuthorizationDetailsManager manager = appLayerFactory.getModelLayerFactory().getAuthorizationDetailsManager();
        Map<String, XMLAuthorizationDetails> xmlAuthorizationDetailsList = manager.getAuthorizationsDetails(collection);
        SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
        Iterator iterator = xmlAuthorizationDetailsList.values().iterator();
        while(iterator.hasNext())  {
            XMLAuthorizationDetails xmlAuthorizationDetails = (XMLAuthorizationDetails) iterator.next();
            buildSolrAuthorizationDetails(xmlAuthorizationDetails, schemasRecordsServices, appLayerFactory);

        }
    }

    private void buildSolrAuthorizationDetails(XMLAuthorizationDetails xmlAuthorizationDetails,
                                               SchemasRecordsServices schemasRecordsServices, AppLayerFactory appLayerFactory)
            throws RecordServicesException {

        SolrAuthorizationDetails solrAuthorizationDetails = schemasRecordsServices.newSolrAuthorizationDetailsWithId(xmlAuthorizationDetails.getId());
        if(xmlAuthorizationDetails.getStartDate() == null || xmlAuthorizationDetails.getStartDate().getYear() < 2007) {
            solrAuthorizationDetails.setStartDate(null);
        }
        else {
            solrAuthorizationDetails.setStartDate(xmlAuthorizationDetails.getStartDate());
        }

        if(xmlAuthorizationDetails.getEndDate() == null || xmlAuthorizationDetails.getEndDate().getYear() < 2007) {
            solrAuthorizationDetails.setEndDate(null);
        }
        else {
            solrAuthorizationDetails.setEndDate(xmlAuthorizationDetails.getEndDate());
        }

        if(!xmlAuthorizationDetails.isSynced()) {
            solrAuthorizationDetails.setSynced(null);
        }
        else {
            solrAuthorizationDetails.setSynced(true);
        }

        solrAuthorizationDetails.setRoles(xmlAuthorizationDetails.getRoles());
        appLayerFactory.getModelLayerFactory().newRecordServices().add(solrAuthorizationDetails);
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
