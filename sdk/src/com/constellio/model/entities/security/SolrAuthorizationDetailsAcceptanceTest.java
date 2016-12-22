package com.constellio.model.entities.security;

import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.PreserveState;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

@PreserveState(state = "C:\\Users\\Constellio\\Desktop\\given_system_in_6.5.33_with_tasks,rm_modules__with_document_rules.zip", enabled = false)
public class SolrAuthorizationDetailsAcceptanceTest extends ConstellioTest {

    @BeforeClass
    public static void tearDown()
            throws Exception {
        //Toggle.FORCE_ROLLBACK.enable();

    }

    @Test
    @MainTestDefaultStart
    public void startApplicationWithSaveState()
            throws Exception {
        RecordPopulateServices.LOG_CONTENT_MISSING = false;
        givenTransactionLogIsEnabled();

        File stateFile = new File(getClass().getAnnotation(PreserveState.class).state());
        getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(stateFile).withPasswordsReset()
                .withFakeEncryptionServices();

        getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(10000);

        UserServices userServices = getModelLayerFactory().newUserServices();
        UserCredential adminCredential = userServices.getUser("admin");
        adminCredential.getServiceKey();

        CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
        List<String> collections = collectionsListManager.getCollections();
        for (String collection : collections) {
            try {
                userServices.getUserInCollection(adminCredential.getUsername(), collection);
            } catch (UserServicesRuntimeException.UserServicesRuntimeException_UserIsNotInCollection e) {
                userServices.addUserToCollection(adminCredential, collection);
            }
        }

        SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
        List<SolrAuthorizationDetails> solrAuthorizationDetailsList = schemasRecordsServices.searchSolrAuthorizationDetailss(LogicalSearchQueryOperators.from(schemasRecordsServices.authorizationDetails.schemaType()).where(Schemas.IDENTIFIER).isNotNull());
        System.out.println(solrAuthorizationDetailsList);


    }
}
