package com.constellio.app.modules.rm.pdfgenerator;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.ContentDaoException;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.DocumentListPDF;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.FileParserException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class PdfGeneratorAsyncAcceptanceTest extends ConstellioTest {
    RMTestRecords records = new RMTestRecords(zeCollection);
    Users users = new Users();
    public static final String CONSOLIDATED_PDF_ID = "consolidatedPdf1";
    public static final String PDF_GENERATOR_ASYNC_ACCEPTANCE_TEST = "consolidated-withoutmetadata.pdf";


    private RMSchemasRecordsServices rmSchemasRecordsServices;

    @Before
    public void setUp() {
        prepareSystem(withZeCollection().withConstellioRMModule()
                .withRMTest(records).withAllTest(users).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent().withDocumentsDecommissioningList());
        rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
    }


    @Test
    public void testDocumentConsolidation() throws ValidationException, ContentDaoException.ContentDaoException_NoSuchContent, IOException, FileParserException {
        List<String> documentIdList = new ArrayList<>();

        documentIdList.add(records.document_A19);
        documentIdList.add(records.document_A49);
        documentIdList.add(records.document_B30);
        documentIdList.add(records.document_B33);
        documentIdList.add(records.document_A79);

        IOServices ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();

        PdfGeneratorAsyncTask pdfGeneratorAsyncTask = new PdfGeneratorAsyncTask(documentIdList,
                "consolidatedPdf1",
                "consolidatedPdf1",
                "consolidatedPdf1",
                users.adminIn(zeCollection).getUsername(), true);

        AsyncTaskExecutionParams params = new AsyncTaskExecutionParams() {
                @Override
                public String getCollection() {
                    return zeCollection;
                }

				@Override
				public void logWarning(String code, Map<String, Object> parameters) {
				}

				@Override
				public void logError(String code, Map<String, Object> parameters) {
				}

                @Override
                public AsyncTaskBatchProcess getBatchProcess() {
                    return null;
                }
        };
        pdfGeneratorAsyncTask.execute(params);

        RecordServices recordServices = getModelLayerFactory().newRecordServices();

        Record resultRecord =  recordServices.getDocumentById(CONSOLIDATED_PDF_ID);

        DocumentListPDF documentListPDF = rmSchemasRecordsServices.wrapDocumentListPdf(resultRecord);

        String hash = documentListPDF.getContent().getLastMajorContentVersion().getHash();
        ContentDao contentDao = getModelLayerFactory().getContentManager().getContentDao();

        FileParser fileParser = getModelLayerFactory().newFileParser();

        InputStream newConsolidatedFileAsInputStream = null;
        ParsedContent parsedContent = null;
        try {
            newConsolidatedFileAsInputStream = contentDao.getContentInputStream(hash, getClass().getSimpleName() + hashCode() + ".PdfGenerator");

            parsedContent = fileParser.parse(newConsolidatedFileAsInputStream, false);
        }
        finally {
            ioServices.closeQuietly(newConsolidatedFileAsInputStream);
        }

        if(parsedContent != null) {
            assertThat(parsedContent.getParsedContent().contains("La racine du mot « grenouille » vient du latin rana," +
                    " voulant dire grenouille, et ranucula ou ranunculus, petite")).isTrue();
            assertThat(parsedContent.getParsedContent().contains("Pour une définition du mot « pêcher »")).isTrue();

            assertThat(parsedContent.getParsedContent().contains("germaniques ont emprunté l'étymon du latin vulgaire : anglais pear (renforcé par le normand),")).isTrue();
            assertThat(parsedContent.getParsedContent().contains("Lynx")).isTrue();
        }
    }
}
