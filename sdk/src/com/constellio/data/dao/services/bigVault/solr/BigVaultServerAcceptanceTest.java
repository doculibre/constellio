package com.constellio.data.dao.services.bigVault.solr;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

public class BigVaultServerAcceptanceTest extends ConstellioTest {
    public static final int WAITING_MILLIS = 60000;
    private RMTestRecords records = new RMTestRecords(zeCollection);
    private RMSchemasRecordsServices rm;
    private RecordServices recordServices;
    private SearchServices ss;

    @Before
    public void setUp() {
        prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule()
                .withAllTestUsers().withRMTest(records).withDocumentsHavingContent()
                .withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
        );
        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        recordServices = getModelLayerFactory().newRecordServices();
        ss = getModelLayerFactory().newSearchServices();
    }

    //TODO Majid Failing @Test
    public void givenSolrServerWhenAskForConfigMangerThenSolrConfigFileCanAccessable() {
        //when
        AtomicFileSystem configManger = getDataLayerFactory().getRecordsVaultServer().getSolrFileSystem();
        //Then
        assertThat(configManger.exists("/solrconfig.xml")).isTrue();
    }

    @Test
    @InDevelopmentTest
    public void testingDelayedCommit() throws Exception {
        String id = "idTestDoc";
        String title = "Document de test 1";
        Document doc = rm.newDocumentWithId(id);
        doc.setTitle(title);
        doc.setFolder(records.getFolder_A04());
        Transaction t = new Transaction();
        t.add(doc);
        t.setRecordFlushing(RecordsFlushing.WITHIN_MILLISECONDS(WAITING_MILLIS));
        long start = new Date().getTime();
        recordServices.execute(t);
        long end = new Date().getTime();
        LogicalSearchCondition condition = from(rm.document.schemaType()).where(rm.document.title()).isEqualTo(title);
        assertThat(rm.wrapDocument(ss.searchSingleResult(condition))).isNull();
        Thread.sleep(WAITING_MILLIS - (end - start));
        assertThat(rm.wrapDocument(ss.searchSingleResult(condition))).isNotNull();

    }
}
