package com.constellio.app.modules.rm.pdfgenerator;

import com.constellio.app.modules.rm.model.PrintableReport.PrintableReportTemplate;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.JasperReportServices;
import com.constellio.app.modules.rm.services.reports.printable.PrintableGeneratorParams;
import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceType;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.app.utils.ReportGeneratorUtils;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.io.ConversionManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.Language;
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
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.users.UserServices;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.data.dao.services.contents.ContentDao.MoveToVaultOption.ONLY_IF_INEXISTING;

public class PdfGeneratorAsyncTask implements AsyncTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfGeneratorAsyncTask.class);

	private List<String> documentIdList;
	private Boolean withMetadata;
	private Boolean asPdfA;
	private String consolidatedName;
	private String consolidatedId;
	private String consolidatedTitle;
	private String username;
	private Locale locale;
	private String languageCode;

	private Content consolidatedContent;


	public static final String GLOBAL_ERROR_KEY = "PdfGeneratorAsyncTask.globalError";
	private static final String TEMP_FILE_RESOURCE_NAME = "PdfGeneratorAsyncTaskTempResourceName";
	private static final String INVALID_SCHEMA_TYPE = "PdfGeneratorAsyncTask.invalidSchemaType";
	private static final String INVALID_PDF_MERGE = "PdfGeneratorAsyncTask.invalidPdfMerge";
	private static final String RECORD_SERVICE_EXCEPTION = "PdfGeneratorAsyncTask.recordServiceExeption";
	private static final String FILE_NOT_FOUND = "PdfGeneratorAsyncTask.FileNotFoundException";
	private static final String INVALID_VISITION = "PdfGeneratorAsyncTask.invalidVisition";
	private static final String TEMPORARY_PDF_DOCUMENT_NAME = "tempraryPdfDocumentName";
	private static final String JASPER_FILE_ERROR = "PdfGeneratorAsyncTask.jasperFileError";
	private static final String NO_PDF_TO_BE_GENERATED = "PdfGeneratorAsyncTask.noPdfToBeGenerated";
	private static final String CANNOT_READ_CONTENT = "PdfGeneratorAsyncTask.cannotReadContent";
	private static final String INCLUDED_PDF_SUCCESSFULLY_READ = "PdfGeneratorAsyncTask.includedPdfSuccessfullyRead";
	private static final String DOCUMENT_INCLUDED_IN_CONSOLIDATED_PDF = "PdfGeneratorAsyncTask.documentIncludedInConsolidatedPdf";

	public static final String READ_CONTENT_FOR_PREVIEW_CONVERSION = "PdfGeneratorAsyncTask-ReadContentForPreviewConversion";

	public PdfGeneratorAsyncTask(List<String> documentIdList, String consolidatedName, String username,
								 String languageCode) {
		this(documentIdList, null, consolidatedName, null, username, false, languageCode);
	}

	public PdfGeneratorAsyncTask(List<String> documentIdList, String consolidatedId,
								 String consolidatedName, String consolidatedTitle,
								 String username, Boolean withMetadata) {
		this(documentIdList, consolidatedId, consolidatedName, consolidatedTitle, username, withMetadata, "fr");
	}

	public PdfGeneratorAsyncTask(List<String> documentIdList, String consolidatedId,
								 String consolidatedName, String consolidatedTitle,
								 String username, Boolean withMetadata, String languageCode) {
		this(documentIdList, consolidatedId, consolidatedName, consolidatedTitle, username, withMetadata, true, languageCode);
	}

	public PdfGeneratorAsyncTask(List<String> documentIdList, String consolidatedId,
								 String consolidatedName, String consolidatedTitle,
								 String username, Boolean withMetadata, Boolean asPdfA, String languageCode) {
		this.documentIdList = documentIdList;
		this.consolidatedId = consolidatedId;
		this.consolidatedName = consolidatedName;
		this.consolidatedTitle = consolidatedTitle;
		this.withMetadata = withMetadata;
		this.asPdfA = asPdfA;
		this.username = username;
		this.languageCode = languageCode;
		this.locale = Language.withCode(languageCode).getLocale();
	}

	private PDDocument getMetadataReport(Document document, ValidationErrors errors, AsyncTaskExecutionParams params)
			throws ValidationException {
		PDDocument result;
		try {
			AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
			String collection = document.getCollection();
			String documentId = document.getId();

			try {
				JasperReportServices jasperReportServices = new JasperReportServices(collection, appLayerFactory);
				PrintableGeneratorParams printableGeneratorParams = PrintableGeneratorParams.builder()
						.XMLDataSourceType(XMLDataSourceType.METADATA)
						.numberOfCopies(1)
						.schemaType(Document.SCHEMA_TYPE)
						.recordIds(Collections.singletonList(documentId))
						.locale(locale)
						.username(username)
						.build();

				List<PrintableReportTemplate> printableReportTemplates = ReportGeneratorUtils.getPrintableReportTemplate(appLayerFactory, collection, document.getSchemaCode(), PrintableReportListPossibleType.DOCUMENT);
				if (!printableReportTemplates.isEmpty()) {
					printableGeneratorParams.setPrintableId(printableReportTemplates.get(0).getId());
				}
				InputStream generatedJasperInputStream = jasperReportServices.generatePrintable(printableGeneratorParams);
				result = PDDocument.load(generatedJasperInputStream);
				result.getDocumentCatalog().setDocumentOutline(null);
			} catch (JRException e) {
				logError(params, document, JASPER_FILE_ERROR);
				LOGGER.error("Error while generating metadata PDF for document id " + document.getId(), e);
				result = null;
			}

		} catch (Throwable t) {
			logError(params, document, JASPER_FILE_ERROR);
			LOGGER.error("Error while generating metadata PDF for document id " + document.getId(), t);
			result = null;
		}
		return result;
	}

	private PDDocument getPreviewWithoutBookmarks(Document document, File tempFolder, AsyncTaskExecutionParams params)
			throws ValidationException {
		PDDocument result;
		try {
			AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
			ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			ContentManager contentManager = modelLayerFactory.getContentManager();
			ConversionManager conversionManager = modelLayerFactory.getDataLayerFactory().getConversionManager();

			Record record = document.getWrappedRecord();
			Content content = document.getContent();
			if (content != null) {
				String hash = document.getContent().getCurrentVersion().getHash();
				String extension = FilenameUtils.getExtension(document.getContent().getCurrentVersion().getFilename());
				if ("pdf".equals(StringUtils.defaultIfBlank(extension, "").toLowerCase())) {
					try (InputStream documentIn = contentManager.getContentInputStream(hash, getClass().getSimpleName() + hash + ".PdfGenerator")) {
						result = PDDocument.load(documentIn);
						result.getDocumentCatalog().setDocumentOutline(null);

						logMessage(params, document, INCLUDED_PDF_SUCCESSFULLY_READ);
					} catch (Throwable t) {
						logError(params, document, CANNOT_READ_CONTENT);
						result = null;
					}
				} else if (modelLayerFactory.getDataLayerFactory().getConversionManager().isSupportedExtension(extension)) {
					InputStream documentPreviewIn;
					if (contentManager.hasContentPreview(hash)) {
						documentPreviewIn = contentManager.getContentPreviewInputStream(hash, getClass().getSimpleName() + hash + ".PdfGenerator");
					} else {
						// The document's preview is about to be generated
						record.set(Schemas.MARKED_FOR_PREVIEW_CONVERSION, true);
						try {
							recordServices.update(record);

							convertContentForPreview(content, conversionManager, tempFolder, modelLayerFactory);
							documentPreviewIn = contentManager.getContentInputStream(hash + ".preview", getClass().getSimpleName() + hash + ".PdfGenerator");
						} catch (RecordServicesException e) {
							documentPreviewIn = null;
							logError(params, document, RECORD_SERVICE_EXCEPTION);
							LOGGER.error("Error while marking record for conversion to true for document id " + document.getId(), e);
						} finally {
							// The system will no longer try to generate a preview for this document
							record.set(Schemas.MARKED_FOR_PREVIEW_CONVERSION, false);
							try {
								recordServices.update(record);
							} catch (RecordServicesException e) {
								logError(params, document, RECORD_SERVICE_EXCEPTION);
								LOGGER.error("Error while marking record for conversion to false for document id " + document.getId(), e);
							}
						}
					}
					// May be null if a RecordServicesException occurred
					if (documentPreviewIn != null) {
						try {
							result = PDDocument.load(documentPreviewIn);
							result.getDocumentCatalog().setDocumentOutline(null);
						} finally {
							IOUtils.closeQuietly(documentPreviewIn);
						}
					} else {
						result = null;
					}
				} else {
					result = null;
				}
			} else {
				result = null;
			}
		} catch (Throwable t) {
			logError(params, document, ExceptionUtils.getStackTrace(t));
			LOGGER.error("Error while generating PDF for document id " + document.getId(), t);
			result = null;
		}

		return result;
	}

	private PDDocument createConsolidatedPdf(List<IncludedDocument> includedPdfDocuments,
											 AsyncTaskExecutionParams params) throws IOException {
		PDDocument consolidatedPdfDocument;
		if (!includedPdfDocuments.isEmpty()) {
			consolidatedPdfDocument = new PDDocument();
			PDDocumentOutline bookmarks = new PDDocumentOutline();
			consolidatedPdfDocument.getDocumentCatalog().setDocumentOutline(bookmarks);

			PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();

			for (IncludedDocument includedDocument : includedPdfDocuments) {
				Document document = includedDocument.document;
				List<PDDocument> pdfDocuments = includedDocument.pdfDocuments;
				for (int index = 0; index < pdfDocuments.size(); index++) {
					PDDocument pdfDocument = pdfDocuments.get(index);
					pdfMergerUtility.appendDocument(consolidatedPdfDocument, pdfDocument);
					if (index == 0) {
						addNewBookmarkToNewPdf(consolidatedPdfDocument, document, pdfDocument);
					}
				}
			}

		} else {
			consolidatedPdfDocument = null;
		}
		return consolidatedPdfDocument;
	}

	private void addNewBookmarkToNewPdf(PDDocument consolidatedPdfDocument, Document document, PDDocument includedPdf) {
		int numberOfPagesInConsolidatedPdf = consolidatedPdfDocument.getNumberOfPages();
		int numberOfPagesInIncludedPdf = includedPdf.getNumberOfPages();
		int indexOfIncludedPdfFirstPage = numberOfPagesInConsolidatedPdf - numberOfPagesInIncludedPdf;
		PDPage includedPdfFirstPage = consolidatedPdfDocument.getPage(indexOfIncludedPdfFirstPage);

		PDPageFitWidthDestination dest = new PDPageFitWidthDestination();
		dest.setPage(includedPdfFirstPage);

		PDOutlineItem bookmark = new PDOutlineItem();
		bookmark.setDestination(dest);

		bookmark.setTitle(document.getTitle());

		PDDocumentOutline bookmarks = consolidatedPdfDocument.getDocumentCatalog().getDocumentOutline();
		bookmarks.addLast(bookmark);
	}

	private void logMessage(AsyncTaskExecutionParams params, Document document, String message) {
		Map<String, Object> messageParams = new HashMap<>();
		messageParams.put("id", document.getId());
		messageParams.put("messageKey", message);
		params.logWarning(document.getId(), messageParams);
	}

	private void logError(AsyncTaskExecutionParams params, Document document, String message)
			throws ValidationException {
		Map<String, Object> messageParams = new HashMap<>();
		messageParams.put("id", document.getId());
		messageParams.put("messageKey", message);

		if (params instanceof PdfGeneratorMergeTaskParam) {
			((PdfGeneratorMergeTaskParam) params).throwError(document.getId(), messageParams);
		} else {
			params.logError(document.getId(), messageParams);
		}
	}

	private void logGlobalError(AsyncTaskExecutionParams params, String message) throws ValidationException {
		Map<String, Object> messageParams = new HashMap<>();
		messageParams.put("id", GLOBAL_ERROR_KEY);
		messageParams.put("messageKey", message);

		if (params instanceof PdfGeneratorMergeTaskParam) {
			((PdfGeneratorMergeTaskParam) params).throwError(GLOBAL_ERROR_KEY, messageParams);
		} else {
			params.logError(GLOBAL_ERROR_KEY, messageParams);
		}
	}

	@Override
	public void execute(AsyncTaskExecutionParams params) throws ValidationException {
		ValidationErrors errors = new ValidationErrors();
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		UserServices userServices = modelLayerFactory.newUserServices();

		ContentManager contentManager = modelLayerFactory.getContentManager();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		RMSchemasRecordsServices schemasRecordsServices = new RMSchemasRecordsServices(params.getCollection(), appLayerFactory);

		List<InputStream> inputStreamList = new ArrayList<>();
		try {
			String collection = params.getCollection();
			File tempFolder = ioServices.newTemporaryFolder(getClass().getName());

			List<IncludedDocument> includedPdfDocuments = new ArrayList<>();
			params.setProgressionUpperLimit(documentIdList.size());
			for (String documentId : documentIdList) {
				Record record = recordServices.getDocumentById(documentId);
				if (!SchemaUtils.getSchemaTypeCode(record.getSchemaCode()).equals(Document.SCHEMA_TYPE)) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("id", documentId);
					parameters.put("schemaCode", SchemaUtils.getSchemaTypeCode(record.getSchemaCode()));
					errors.add(PdfGeneratorAsyncTask.class, INVALID_SCHEMA_TYPE, parameters);
					errors.throwIfNonEmpty();
				}

				Document document = schemasRecordsServices.wrapDocument(record);
				List<PDDocument> includedPdfDocumentsForCurrentDocument = new ArrayList<>();

				if (withMetadata) {
					PDDocument metadataReport = getMetadataReport(document, errors, params);
					if (metadataReport != null) {
						includedPdfDocumentsForCurrentDocument.add(metadataReport);
					}
				}

				PDDocument previewWithoutBookmarks = getPreviewWithoutBookmarks(document, tempFolder, params);
				if (previewWithoutBookmarks != null) {
					includedPdfDocumentsForCurrentDocument.add(previewWithoutBookmarks);
				} else {
					logError(params, document, CANNOT_READ_CONTENT);
				}

				if (!includedPdfDocumentsForCurrentDocument.isEmpty()) {
					includedPdfDocuments.add(new IncludedDocument(document, includedPdfDocumentsForCurrentDocument));
					logMessage(params, document, DOCUMENT_INCLUDED_IN_CONSOLIDATED_PDF);
				}
				params.incrementProgression(1);
			}

			PDDocument consolidatedPdf = createConsolidatedPdf(includedPdfDocuments, params);
			if (consolidatedPdf != null) {
				DocumentListPDF documentListPDF;

				File consolidatedPdfFile = ioServices.newTemporaryFile(getClass().getName() + ".pdf");
				consolidatedPdf.save(consolidatedPdfFile);

				if (asPdfA) {
					File newConsolidatedPdfFile;
					try (InputStream is = new FileInputStream(consolidatedPdfFile)) {
						ConversionManager conversionManager = modelLayerFactory.getDataLayerFactory().getConversionManager();
						newConsolidatedPdfFile = conversionManager.convertToPDF(is, consolidatedName, tempFolder, asPdfA);
						ioServices.deleteQuietly(consolidatedPdfFile);
						consolidatedPdfFile = newConsolidatedPdfFile;
					}
				}

				try (InputStream resultInputStream = new FileInputStream(consolidatedPdfFile)) {
					consolidatedContent =
							createContent(resultInputStream, consolidatedName, contentManager,
									userServices.getUserInCollection(username, collection));

					if (!StringUtils.isBlank(consolidatedId)) {
						documentListPDF =
								newDocumentListPdfWithContent(consolidatedId, consolidatedTitle, consolidatedContent,
										userServices.getUserInCollection(username, collection), schemasRecordsServices);

						recordServices.add(documentListPDF);
					}
				} finally {
					ioServices.closeQuietly(consolidatedPdf);
					for (IncludedDocument includedDocument : includedPdfDocuments) {
						for (PDDocument pdfDocument : includedDocument.pdfDocuments) {
							ioServices.closeQuietly(pdfDocument);
						}
					}
					ioServices.deleteQuietly(consolidatedPdfFile);
					ioServices.deleteDirectory(tempFolder);
				}
			} else {
				errors.add(PdfGeneratorAsyncTask.class, NO_PDF_TO_BE_GENERATED);
				logGlobalError(params, NO_PDF_TO_BE_GENERATED);
			}
		} catch (FileNotFoundException e) {
			errors.add(PdfGeneratorAsyncTask.class, FILE_NOT_FOUND);
			errors.throwIfNonEmpty();
		} catch (IOException e) {
			errors.add(PdfGeneratorAsyncTask.class, INVALID_PDF_MERGE);
			errors.throwIfNonEmpty();
		} catch (RecordServicesException e) {
			errors.add(PdfGeneratorAsyncTask.class, RECORD_SERVICE_EXCEPTION);
			errors.throwIfNonEmpty();
		} catch (RecordServicesRuntimeException e) {
			errors.add(PdfGeneratorAsyncTask.class, RECORD_SERVICE_EXCEPTION);
			errors.throwIfNonEmpty();
		} finally {
			for (InputStream inputStream : inputStreamList) {
				ioServices.closeQuietly(inputStream);
			}
		}
	}

	public ContentVersionDataSummary upload(InputStream resource, String fileName, ContentManager contentManager) {
		return contentManager.upload(resource, new ContentManager.UploadOptions(fileName)).getContentVersionDataSummary();
	}

	private Content createContent(InputStream inputStream, String fileName, ContentManager contentManager, User user) {
		ContentVersionDataSummary version01 = upload(inputStream, fileName, contentManager);
		return contentManager.createMajor(user, fileName, version01);
	}

	private DocumentListPDF newDocumentListPdfWithContent(String id, String title, Content content, User user,
														  RMSchemasRecordsServices rmSchemasRecordsServices) {
		DocumentListPDF documentListPDF = rmSchemasRecordsServices.newDocumentListPDFWithId(id);
		documentListPDF.setTitle(title).setContent(content);
		documentListPDF.setCreatedBy(user.getId());
		return documentListPDF;
	}

	private void convertContentForPreview(Content content, ConversionManager conversionManager, File tempFolder,
										  ModelLayerFactory modelLayerFactory) {
		String hash = content.getCurrentVersion().getHash();
		String filename = content.getCurrentVersion().getFilename();
		ContentDao contentDao = modelLayerFactory.getDataLayerFactory().getContentsDao();
		InputStream inputStream = null;
		try {
			inputStream = contentDao.getContentInputStream(hash, READ_CONTENT_FOR_PREVIEW_CONVERSION);
			File file = conversionManager.convertToPDF(inputStream, filename, tempFolder);
			contentDao.moveFileToVault(hash + ".preview", file, ONLY_IF_INEXISTING);
		} catch (Throwable t) {
			LOGGER.warn("Cannot convert content '" + filename + "' with hash '" + hash + "'", t);
		} finally {
			modelLayerFactory.getIOServicesFactory().newIOServices().closeQuietly(inputStream);
		}
	}

	public String getConsolidatedName() {
		return consolidatedName;
	}

	public String getConsolidatedTitle() {
		return consolidatedTitle;
	}

	public String getUserName() {
		return username;
	}

	public Content getConsolidatedContent() {
		return consolidatedContent;
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[]{documentIdList, consolidatedId, consolidatedName, consolidatedTitle, username, withMetadata,
							asPdfA, languageCode};
	}


	private static class IncludedDocument {

		private Document document;

		private List<PDDocument> pdfDocuments;

		public IncludedDocument(Document document, List<PDDocument> pdfDocuments) {
			super();
			this.document = document;
			this.pdfDocuments = pdfDocuments;
		}

	}

}

