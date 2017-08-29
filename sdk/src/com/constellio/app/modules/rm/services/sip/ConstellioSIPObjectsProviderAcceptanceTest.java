package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.ui.framework.buttons.SIPButton.SIPBuildAsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.sdk.tests.TestUtils.asList;

public class ConstellioSIPObjectsProviderAcceptanceTest extends ConstellioTest {
    RMTestRecords records = new RMTestRecords(zeCollection);
    SearchServices searchServices;
    RMSchemasRecordsServices rm;
    RecordServices recordServices;
    ContentManager contentManager;

    @Before
    public void setup(){
        prepareSystem(
                withZeCollection().withConstellioRMModule().withRMTest(records)
                        .withFoldersAndContainersOfEveryStatus().withAllTestUsers()
        );
        this.searchServices = getModelLayerFactory().newSearchServices();
        this.rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        this.contentManager = getModelLayerFactory().getContentManager();
        this.recordServices = getModelLayerFactory().newRecordServices();
    }

    @Test
    public void testSIPGenerationWithEmail() throws Exception {
        File emailFile = getTestResourceFile("testFile.msg");
        ContentVersionDataSummary summary = contentManager.upload(emailFile);
        Email email = rm.newEmail();
        email.setContent(contentManager.createMajor(records.getAdmin(), "emailTest", summary));
        email.setFolder(records.getFolder_A01());
        Transaction transaction = new Transaction();
        transaction.add(email);
        recordServices.execute(transaction);

        SIPBuildAsyncTask task = new SIPBuildAsyncTask("testSIPFile", asList("test1", "test2"), Collections.singletonList(email.getId()), Collections.<String>emptyList(), false, records.getAdmin().getUsername(), false, getAppLayerFactory().newApplicationService().getWarVersion());
        getAppLayerFactory().getModelLayerFactory().getBatchProcessesManager().addAsyncTask(new AsyncTaskCreationRequest(task, zeCollection, "SIPArchive from test com.constellio.app.modules.rm.services.sip.SIPBuildAsyncTaskAcceptanceTest"));
        waitForBatchProcess();

        MetadataSchema sipArchiveSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType(SIParchive.SCHEMA_TYPE).getCustomSchema(SIParchive.SCHEMA_NAME);
        LogicalSearchCondition allCondition = LogicalSearchQueryOperators.from(sipArchiveSchema).where(ALL);
        SIParchive records = rm.wrapSIParchive(searchServices.searchSingleResult(allCondition));
        Content zipContent = records.getContent();
        zipContent.getLastMajorContentVersion().getHash();
    }

}
