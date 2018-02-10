package com.constellio.app.modules.rm.pdfgenerator;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.JasperPdfGenerator;
import com.constellio.app.modules.rm.services.reports.XmlReportGenerator;
import com.constellio.app.modules.rm.services.reports.parameters.XmlReportGeneratorParameters;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.io.ConversionManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.DocumentListPDF;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.users.UserServices;
import net.sf.jasperreports.engine.JRException;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class PdfGeneratorAsyncTask implements AsyncTask {

    private List<String> documentIdList;
    private Boolean withMetadata;
    private  String consolidatedName;
    private String consolidedId;
    private String consolidatedTitle;
    private String userName;

    private static final String TEMP_FILE_RESOURCE_NAME = "PdfGeneratorAsyncTaskTempResourceName";
    private static final String INVALID_SCHEMA_TYPE = "PdfGeneratorAsyncTask.invalidSchemaType";
    private static final String INVALID_PDF_MERGE = "PdfGeneratorAsyncTask.invalidPdfMerge";
    private static final String RECORD_SERVICE_EXCEPTION = "PdfGeneratorAsyncTask.recordServiceExeption";
    private static final String FILE_NOT_FOUND = "PdfGeneratorAsyncTask.FileNotFoundException";
    private static final String INVALID_VISITION = "PdfGeneratorAsyncTask.invalidVisition";
    private static final String TEMPORARY_PDF_DOCUMENT_NAME = "tempraryPdfDocumentName";
    private static final String JASPER_FILE_ERROR = "PdfGeneratorAsyncTask.jasperFileError";


    public static final String READ_CONTENT_FOR_PREVIEW_CONVERSION = "PdfGeneratorAsyncTask-ReadContentForPreviewConversion";
    private static final Logger LOGGER = LoggerFactory.getLogger(PdfGeneratorAsyncTask.class);

    private PdfGeneratorProgressInfo progressInfo = new PdfGeneratorProgressInfo();

    public PdfGeneratorAsyncTask(List<String> documentIdList, String consolidedId,
                                 String consolidatedName, String consolidatedTitle,
                                 String userName, Boolean withMetadata) {
        this.documentIdList = documentIdList;
        this.consolidedId = consolidedId;
        this.consolidatedName = consolidatedName;
        this.consolidatedTitle = consolidatedTitle;
        this.withMetadata = withMetadata;
        this.userName = userName;
    }

    @Override
    public void execute(AsyncTaskExecutionParams params) throws ValidationException {
        progressInfo.notifyStartProcessing();

        ValidationErrors errors = new ValidationErrors();
        AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
        ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
        IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
        UserServices userServices = modelLayerFactory.newUserServices();
        ContentDao contentDao = modelLayerFactory.getDataLayerFactory().getContentsDao();

        ContentManager contentManager = modelLayerFactory.getContentManager();
        RecordServices recordServices = modelLayerFactory.newRecordServices();
        RMSchemasRecordsServices schemasRecordsServices = new RMSchemasRecordsServices(params.getCollection(), appLayerFactory);
        List<Document> documentList = new ArrayList<>();
        ConversionManager conversionManager = modelLayerFactory.getDataLayerFactory().getConversionManager();
        List<File> temporaryPdfFile = new ArrayList<>();

        PDFMergerUtility ut = new PDFMergerUtility();
        List<InputStream> inputStreamList = new ArrayList<>();
        int number = 0;

        try {

            number++;
             for(final String documentId : documentIdList) {
                 progressInfo.notifyGlobalProcessingMessage("Processing file: " + number + "/" + documentIdList.size());

                Record record = recordServices.getDocumentById(documentId);
                if (!SchemaUtils.getSchemaTypeCode(record.getSchemaCode()).equals(Document.SCHEMA_TYPE)) {
                     Map<String, Object> parameters = new HashMap<>();
                     parameters.put("schemaCode", SchemaUtils.getSchemaTypeCode(record.getSchemaCode()));
                     errors.add(PdfGeneratorAsyncTask.class, INVALID_SCHEMA_TYPE, parameters);
                     errors.throwIfNonEmpty();
                 }

                final File tempFolder = ioServices.newTemporaryFolder("previewConversion");
                Document document = schemasRecordsServices.wrapDocument(record);
                progressInfo.notifyFileProcessingMessage(document.getTitle(), "Processing...");

                documentList.add(document);
                Content content = document.getContent();
                String hash = document.getContent().getCurrentVersion().getHash();

                InputStream in;
                if (contentManager.hasContentPreview(hash)) {
                     in = contentManager.getContentPreviewInputStream(hash, getClass().getSimpleName() + hash + ".PdfGenerator");
                 } else {
                     record.set(Schemas.MARKED_FOR_PREVIEW_CONVERSION, true);
                     recordServices.update(record);
                     
                     try {
                         convertContentForPreview(content, conversionManager,tempFolder, modelLayerFactory);
                         in = contentManager.getContentInputStream(hash + ".preview", getClass().getSimpleName() + hash + ".PdfGenerator");
                     } finally {
                         record.set(Schemas.MARKED_FOR_PREVIEW_CONVERSION, false);
                         recordServices.update(record);
                     }
                 }
                 inputStreamList.add(in);
                 PDDocument pdDocument = PDDocument.load(in);
                 pdDocument.getDocumentCatalog().setDocumentOutline(null);


                 File file = ioServices.newTemporaryFile("PdTemporaryFile" + number);
                 temporaryPdfFile.add(file);

                 pdDocument.save(file);
                 pdDocument.close();

                 contentDao.moveFileToVault(file, hash + ".preview");

                 InputStream inputStream2 = contentManager.getContentInputStream(hash + ".preview", getClass().getSimpleName() + hash + "signet" + ".PdfGenerator");


                 inputStreamList.add(inputStream2);
                 if(withMetadata) {

                     XmlReportGeneratorParameters xmlGeneratorParameters = new XmlReportGeneratorParameters(
                             1);

                     XmlReportGenerator xmlReportGenerator = new XmlReportGenerator(appLayerFactory, params.getCollection(),
                             xmlGeneratorParameters);

                     ArrayList<String> documentIdAsString = new ArrayList<>();

                     documentIdAsString.add(documentId);

                     xmlGeneratorParameters.setElementWithIds(Document.SCHEMA_TYPE, documentIdAsString);


                     JasperPdfGenerator jasperPdfGenerator = new JasperPdfGenerator(xmlReportGenerator);

                     File jasperFile = new File(new FoldersLocator().getModuleResourcesFolder("rm"), "JasperPdfMetadataReport.jasper");
                     File generatedJasperFile = null;
                     try {
                         generatedJasperFile = jasperPdfGenerator.createPDFFromXmlAndJasperFile(jasperFile);
                     } catch (JRException e) {
                         errors.add(PdfGeneratorAsyncTask.class, JASPER_FILE_ERROR);
                         errors.throwIfNonEmpty();
                     }

                     ut.addSource(generatedJasperFile);
                 }

                 ut.addSource(inputStream2);

                 progressInfo.notifyFileProcessingMessage(document.getTitle(), "Finished.");
            }

            File fileTemporaryFile = ioServices.newTemporaryFile(TEMP_FILE_RESOURCE_NAME);
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileTemporaryFile)) {
                ut.setDestinationStream(fileOutputStream);
                ut.mergeDocuments();
            }

            DocumentListPDF documentListPDF;
            try (InputStream resultInputStream = new FileInputStream(fileTemporaryFile)) {
                documentListPDF = newDocumentListPdfWithContent(consolidedId, consolidatedTitle, resultInputStream, consolidatedName,
                        contentManager, userServices.getUserInCollection(userName,
                                params.getCollection()), schemasRecordsServices);

                recordServices.add(documentListPDF);
            }

            contentDao.moveFileToVault(fileTemporaryFile, documentListPDF.getContent().getLastMajorContentVersion().getHash());

            temporaryPdfFile.add(fileTemporaryFile);

            for(File file : temporaryPdfFile)  {
                ioServices.deleteQuietly(file);
            }
        } catch (FileNotFoundException e) {
            errors.add(PdfGeneratorAsyncTask.class, FILE_NOT_FOUND);
            errors.throwIfNonEmpty();
        } catch (IOException e) {
            errors.add(PdfGeneratorAsyncTask.class, INVALID_PDF_MERGE);
            errors.throwIfNonEmpty();
        } catch (COSVisitorException e) {
            errors.add(PdfGeneratorAsyncTask.class, INVALID_VISITION);
            errors.throwIfNonEmpty();
        } catch (RecordServicesException e) {
            errors.add(PdfGeneratorAsyncTask.class, RECORD_SERVICE_EXCEPTION);
        } finally {
            for (InputStream inputStream : inputStreamList) {
                ioServices.closeQuietly(inputStream);
            }

            progressInfo.notifyEndProcessing();
        }
    }

    public ContentVersionDataSummary upload(InputStream resource, String fileName, ContentManager contentManager) {
        return contentManager.upload(resource, new ContentManager.UploadOptions(fileName)).getContentVersionDataSummary();
    }

    private DocumentListPDF newDocumentListPdfWithContent(String id, String title, InputStream inputStream, String fileName, ContentManager contentManager, User user, RMSchemasRecordsServices rmSchemasRecordsServices) {
        ContentVersionDataSummary version01 = upload(inputStream, fileName, contentManager);
        Content content = contentManager.createMajor(user, fileName, version01);

        DocumentListPDF documentListPDF = rmSchemasRecordsServices.newDocumentListPDFWithId(id);
        documentListPDF.setTitle(title).setContent(content);
        return documentListPDF;
    }

    private void convertContentForPreview(Content content, ConversionManager conversionManager, File tempFolder, ModelLayerFactory modelLayerFactory) {
        String hash = content.getCurrentVersion().getHash();
        String filename = content.getCurrentVersion().getFilename();
        ContentDao contentDao = modelLayerFactory.getDataLayerFactory().getContentsDao();
        InputStream inputStream = null;
        try {
            inputStream = contentDao.getContentInputStream(hash, READ_CONTENT_FOR_PREVIEW_CONVERSION);
            File file = conversionManager.convertToPDF(inputStream, filename, tempFolder);
            contentDao.moveFileToVault(file, hash + ".preview");
        } catch (Throwable t) {
            LOGGER.warn("Cannot convert content '" + filename + "' with hash '" + hash + "'", t);
        } finally {
            modelLayerFactory.getIOServicesFactory().newIOServices().closeQuietly(inputStream);
        }
    }

    public PdfGeneratorProgressInfo getProgressInfo() {
        return progressInfo;
    }

    public String getConsolidatedName() {
        return consolidatedName;
    }

    public String getConsolidatedTitle() {
        return consolidatedTitle;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public Object[] getInstanceParameters() {
        return new Object[] {documentIdList, consolidedId, consolidatedName, consolidatedTitle, userName, withMetadata};
    }
}
