package com.constellio.app.modules.rm.services.sip;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.wrappers.Document;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.sip.data.intelligid.ConstellioSIPObjectsProvider;
import com.constellio.app.modules.rm.services.sip.filter.SIPFilter;
import com.constellio.app.modules.rm.services.sip.model.EntityRetriever;
import com.constellio.app.modules.rm.services.sip.model.SIPDocument;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;

public class ConstellioSIPObjectsProviderAcceptanceTest extends ConstellioTest {
    RMTestRecords records = new RMTestRecords(zeCollection);
    SearchServices searchServices;
    RMSchemasRecordsServices rm;
    RecordServices recordServices;
    ContentManager contentManager;
    IOServices ioServices;
    EntityRetriever entityRetriever;
    List<Metadata> folderMetadata, documentMetadata;

    SIPFilter sipFilter;
    ConstellioSIPObjectsProvider objectsProvider;
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
        this.ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
        this.sipFilter = new SIPFilter(zeCollection, getAppLayerFactory()).withIncludeFolderIds(Collections.singletonList(records.getFolder_A01().getId()));
        this.objectsProvider = new ConstellioSIPObjectsProvider(zeCollection, getAppLayerFactory(), sipFilter);
        this.entityRetriever = new EntityRetriever(zeCollection, getAppLayerFactory());
    }

    @Test
    public void testMetadataGettingFromDocumentContainsOnlyOne(){
        Document document = rm.newDocument();
        SIPDocument sipDocument = new SIPDocument(document, document.getSchema().getMetadatas(), this.entityRetriever);
        assertThat(this.objectsProvider.getMetadataIds(sipDocument)).containsOnly("typeDocument");
    }

    @Test
    public void testMetadataGettingFromEmailContainsCorrectMetadata(){
        Email email = rm.newEmail();
        SIPDocument sipDocument = new SIPDocument(email, email.getSchema().getMetadatas(), this.entityRetriever);
        assertThat(this.objectsProvider.getMetadataIds(sipDocument)).containsOnly(Email.EMAIL_TO, Email.EMAIL_FROM, Email.EMAIL_IN_NAME_OF, Email.EMAIL_CC_TO, Email.EMAIL_BCC_TO, Email.EMAIL_OBJECT);
    }

    @Test
    public void testThatEmailReturnsJoinFiles() throws Exception {
        SIPDocument sipDocument = null;
        InputStream fileinputstream = null;
        try{
            File emailFile = getTestResourceFile("testFile.msg");
            ContentVersionDataSummary summary = contentManager.upload(emailFile);
            String emailFileName = "emailTest.msg";
            Email email = rm.newEmail();
            email.setContent(contentManager.createMajor(records.getAdmin(), emailFileName, summary));
            email.setFolder(records.getFolder_A01());
            Transaction transaction = new Transaction();
            transaction.add(email);
            recordServices.execute(transaction);
            sipDocument = new SIPDocument(email, email.getSchema().getMetadatas(), this.entityRetriever);
            fileinputstream = newFileInputStream(emailFile);
            assertThat(objectsProvider.getExtraFiles(sipDocument).keySet()).hasSize(((Map) rm.parseEmail(emailFile.getName(), fileinputstream).get("attachments")).size());
        } finally {
            if(sipDocument != null) {
                ioServices.deleteQuietly(sipDocument.getFile());
                ioServices.closeQuietly(fileinputstream);
            }
        }
    }

    @Test
    public void testGetMetadataValues() throws Exception {
        SIPDocument sipDocument = null;
        InputStream fileinputstream = null;
        try{
            String emailTo = "emailToTest";
            String emailFrom = "emailFromTest";
            String emailInNameOf = "emailInNameOf";
            String emailCCTo = "emailCCTo";
            String emailBCCTo = "emailBCCTo";
            String emailObject = "emailObjectTEst";
            File emailFile = getTestResourceFile("testFile.msg");
            ContentVersionDataSummary summary = contentManager.upload(emailFile);
            String emailFileName = "emailTest.msg";
            Email email = rm.newEmail();
            email.setEmailTo(asList(emailTo));
            email.setEmailFrom(emailFrom);
            email.setEmailInNameOf(emailInNameOf);
            email.setEmailCCTo(asList(emailCCTo));
            email.setEmailBCCTo(asList(emailBCCTo));
            email.setEmailObject(emailObject);
            email.setContent(contentManager.createMajor(records.getAdmin(), emailFileName, summary));
            email.setFolder(records.getFolder_A01());
            Transaction transaction = new Transaction();
            transaction.add(email);
            recordServices.execute(transaction);
            sipDocument = new SIPDocument(email, email.getSchema().getMetadatas(), this.entityRetriever);
            fileinputstream = newFileInputStream(emailFile);
            assertThat(objectsProvider.getMetadataValues(sipDocument, "emailTo").get(0)).isEqualTo(email.getEmailTo().get(0));
            assertThat(objectsProvider.getMetadataValues(sipDocument, "emailFrom").get(0)).isEqualTo(email.getEmailFrom());
            assertThat(objectsProvider.getMetadataValues(sipDocument, "emailInNameOf").get(0)).isEqualTo(email.getEmailInNameOf());
            assertThat(objectsProvider.getMetadataValues(sipDocument, "emailCCTo").get(0)).isEqualTo(email.getEmailCCTo().get(0));
            assertThat(objectsProvider.getMetadataValues(sipDocument, "emailBCCTo").get(0)).isEqualTo(email.getEmailBCCTo().get(0));
            assertThat(objectsProvider.getMetadataValues(sipDocument, "emailObject").get(0)).isEqualTo(email.getEmailObject());
        } finally {
            if(sipDocument != null) {
                ioServices.deleteQuietly(sipDocument.getFile());
                ioServices.closeQuietly(fileinputstream);
            }
        }
    }

    @Test
    public void testIfListReturnListOfObjects(){
        List<String> documentsIds = new ArrayList<>();
        MetadataSchemaType documentType = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType(com.constellio.app.modules.rm.wrappers.Document.SCHEMA_TYPE);
        LogicalSearchCondition condition = LogicalSearchQueryOperators.from(documentType)
                .where(documentType.getDefaultSchema().getMetadata(com.constellio.app.modules.rm.wrappers.Document.FOLDER))
                .isEqualTo(records.getFolder_A01());
        List<Record>  documentsRecords = searchServices.search(new LogicalSearchQuery(condition));
        for(Record record : documentsRecords) {
            documentsIds.add(record.getId());
        }
        assertThat(this.objectsProvider.list()).extracting("id").containsOnly(documentsIds.toArray(new String[0]));
    }

    private String[] getCodeFromMetadatas(List<Metadata> list){
        List<String> codes = new ArrayList<>();
        for(Metadata metadata : list) {
            codes.add(metadata.getCode());
        }
        return codes.toArray(new String[0]);
    }
}
