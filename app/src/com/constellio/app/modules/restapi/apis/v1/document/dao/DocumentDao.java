package com.constellio.app.modules.restapi.apis.v1.document.dao;

import com.constellio.app.modules.restapi.apis.v1.document.dto.ContentDto;
import com.constellio.app.modules.restapi.apis.v1.document.dto.DocumentContentDto;
import com.constellio.app.modules.restapi.apis.v1.document.dto.DocumentDto;
import com.constellio.app.modules.restapi.apis.v1.document.exception.DocumentContentNotFoundException;
import com.constellio.app.modules.restapi.apis.v1.resource.dao.ResourceDao;
import com.constellio.app.modules.restapi.core.exception.CannotReadContentException;
import com.constellio.app.modules.restapi.core.exception.ConsolidationException;
import com.constellio.app.modules.restapi.core.exception.OptimisticLockRuntimeException;
import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.exception.UnresolvableOptimisticLockException;
import com.constellio.app.modules.restapi.document.exception.InvalidConsolidationSchemaTypeException;
import com.constellio.app.modules.rm.pdfgenerator.PdfGeneratorAsyncTask;
import com.constellio.app.modules.rm.pdfgenerator.PdfGeneratorMergeTaskParam;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentImplRuntimeException;
import com.constellio.model.services.contents.ContentManager.ContentVersionDataSummaryResponse;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentManagerRuntimeException;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;

import java.io.InputStream;
import java.util.List;

import static com.constellio.app.modules.restapi.apis.v1.document.enumeration.VersionType.MAJOR;
import static com.constellio.app.modules.restapi.apis.v1.document.enumeration.VersionType.MINOR;

public class DocumentDao extends ResourceDao {

	private static final String LAST_VERSION = "last";

	public Record createDocument(User user, MetadataSchema documentSchema, DocumentDto document, Content content,
								 String flush) throws Exception {
		Transaction transaction = buildTransaction(flush, user);

		Record documentRecord = recordServices.newRecordWithSchema(documentSchema);

		Record documentTypeRecord = getResourceTypeRecord(document.getType(), documentSchema.getCollection());

		updateDocumentMetadataValues(documentRecord, documentTypeRecord, documentSchema, document, content, false);
		updateMetadataValues(documentRecord, documentSchema, document.getAttributes(), false);
		updateCustomMetadataValues(documentRecord, documentSchema, document.getExtendedAttributes(), false);

		transaction.add(documentRecord);

		recordServices.execute(transaction);

		return documentRecord;
	}

	public Record updateDocument(User user, Record documentRecord, MetadataSchema documentSchema, DocumentDto document,
								 Content content, boolean partial, String flushMode) throws Exception {
		Transaction transaction = buildTransaction(flushMode, user);

		if (document.getETag() != null) {
			transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		}

		if (!partial || document.getType() != null) {
			MetadataSchema oldDocumentSchema = getMetadataSchema(documentRecord);
			if (!documentSchema.getCode().equals(oldDocumentSchema.getCode())) {
				documentRecord.changeSchema(oldDocumentSchema, documentSchema);
			}
		}

		Record documentTypeRecord;
		if (partial && document.getType() == null) {
			String documentTypeId = getMetadataValue(documentRecord, Document.TYPE);
			documentTypeRecord = documentTypeId != null ? getRecordById(documentTypeId) : null;
		} else {
			documentTypeRecord = getResourceTypeRecord(document.getType(), documentRecord.getCollection());
		}

		updateDocumentMetadataValues(documentRecord, documentTypeRecord, documentSchema, document, content, partial);
		updateCustomMetadataValues(documentRecord, documentSchema, document.getExtendedAttributes(), partial);

		transaction.add(documentRecord);

		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException.UnresolvableOptimisticLockingConflict e) {
			throw new UnresolvableOptimisticLockException(document.getId());
		} catch (RecordServicesException.OptimisticLocking e) {
			throw new OptimisticLockRuntimeException(document.getId(), document.getETag(), e.getVersion());
		}

		return documentRecord;
	}

	public DocumentContentDto getContent(Record documentRecord, String version) {
		try {
			Content content = getMetadataValue(documentRecord, Document.CONTENT);
			if (content == null) {
				throw new DocumentContentNotFoundException(documentRecord.getId(), version);
			}

			ContentVersion contentVersion = version.equals(LAST_VERSION) ?
											content.getCurrentVersion() :
											content.getVersion(version);
			InputStream stream = contentManager.getContentInputStream(contentVersion.getHash(), contentVersion.getFilename());

			String mimeType = contentVersion.getMimetype();

			return DocumentContentDto.builder().content(stream).mimeType(mimeType).filename(contentVersion.getFilename()).build();
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId |
				ContentImplRuntimeException.ContentImplRuntimeException_NoSuchVersion |
				ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent e) {
			throw new DocumentContentNotFoundException(documentRecord.getId(), version);
		}
	}

	public void deleteContent(Content content) {
		contentManager.markForDeletionIfNotReferenced(content.getCurrentVersion().getHash());
	}

	public void deleteDocument(User user, Record documentRecord, boolean physical) {
		Boolean logicallyDeleted = documentRecord.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS);

		if (physical) {
			if (!Boolean.TRUE.equals(logicallyDeleted)) {
				recordServices.logicallyDelete(documentRecord, user);
			}
			recordServices.physicallyDelete(documentRecord, user);
		} else {
			if (Boolean.TRUE.equals(logicallyDeleted)) {
				throw new RecordLogicallyDeletedException(documentRecord.getId());
			}
			recordServices.logicallyDelete(documentRecord, user);
		}
	}

	public Content uploadContent(User user, Record documentRecord, ContentDto documentContent,
								 InputStream contentInputStream) {
		boolean isMajor = documentContent.getVersionType() == MAJOR;

		String filename = documentContent.getFilename();
		ContentVersionDataSummaryResponse contentResponse = contentManager.upload(contentInputStream,
				new UploadOptions(false, null, false, filename));

		Content content = documentRecord != null ? this.<Content>getMetadataValue(documentRecord, Document.CONTENT) : null;
		if (content != null) {
			if (filename == null) {
				return content.updateContent(user, contentResponse.getContentVersionDataSummary(), isMajor);
			}
			return content.updateContentWithName(user, contentResponse.getContentVersionDataSummary(), isMajor, filename);
		}

		if (filename == null) {
			throw new RequiredParameterException("content.filename");
		}

		if (isMajor) {
			return contentManager.createMajor(user, filename, contentResponse.getContentVersionDataSummary());
		}
		return contentManager.createMinor(user, filename, contentResponse.getContentVersionDataSummary());
	}

	public Content mergeContent(DocumentDto document, List<String> mergeSourceIds, String collection, User user) {
		String title = document.getTitle();
		if (!title.endsWith(".pdf")) {
			title += ".pdf";
		}
		String language = ConstellioFactories.getInstance().getModelLayerConfiguration().getMainDataLanguage();
		PdfGeneratorAsyncTask task = new PdfGeneratorAsyncTask(mergeSourceIds, null, title, null,
				user.getUsername(), false, false, language);

		Content content = null;
		try {
			task.execute(new PdfGeneratorMergeTaskParam(collection));
			content = task.getConsolidatedContent();

			if (content == null) {
				throw new ConsolidationException();
			}

			ContentDto contentDto = ContentDto.builder()
					.filename(content.getCurrentVersion().getFilename())
					.versionType(content.getCurrentVersion().isMajor() ? MAJOR : MINOR)
					.version(content.getCurrentVersion().getVersion())
					.hash(content.getCurrentVersion().getHash())
					.build();
			document.setContent(contentDto);
		} catch (ValidationException e) {
			ValidationErrors validationErrors = e.getValidationErrors();
			if (validationErrors == null) {
				throw new ConsolidationException();
			}

			List<ValidationError> errors = validationErrors.getValidationErrors();
			if (errors == null || errors.isEmpty()) {
				throw new ConsolidationException();
			}

			ValidationError error = errors.get(0);
			if (error == null) {
				throw new ConsolidationException();
			}

			String documentId = (String) error.getParameter("id");
			String schemaCode = (String) error.getParameter("schemaCode");
			if (documentId != null && schemaCode != null
				&& error.getValidatorErrorCode().equals(InvalidConsolidationSchemaTypeException.CODE)) {
				throw new InvalidConsolidationSchemaTypeException(documentId, schemaCode);
			}
			String msg = (String) error.getParameter("messageKey");
			if (documentId == null || msg == null || !msg.equals(CannotReadContentException.CODE)) {
				throw new ConsolidationException();
			}

			throw new CannotReadContentException(documentId);
		}

		return content;
	}

	private void updateDocumentMetadataValues(Record documentRecord, Record documentTypeRecord, MetadataSchema schema,
											  DocumentDto document, Content content, boolean partial) {
		updateDocumentMetadataValue(documentRecord, schema, Document.FOLDER, document.getFolderId(), partial);
		updateDocumentMetadataValue(documentRecord, schema, Document.TITLE, document.getTitle(), partial);
		updateDocumentMetadataValue(documentRecord, schema, Document.KEYWORDS, document.getKeywords(), partial);
		updateDocumentMetadataValue(documentRecord, schema, Document.AUTHOR, document.getAuthor(), partial);
		updateDocumentMetadataValue(documentRecord, schema, Document.SUBJECT, document.getSubject(), partial);
		updateDocumentMetadataValue(documentRecord, schema, Document.COMPANY, document.getOrganization(), partial);

		String documentTypeId = documentTypeRecord != null ? documentTypeRecord.getId() : null;
		updateMetadataValue(documentRecord, schema, Document.TYPE, documentTypeId);

		if (content != null) {
			updateMetadataValue(documentRecord, schema, Document.CONTENT, content);
		}
	}

	@Override
	protected String getResourceSchemaType() {
		return Document.SCHEMA_TYPE;
	}

	@Override
	protected String getResourceTypeSchemaType() {
		return DocumentType.SCHEMA_TYPE;
	}
}