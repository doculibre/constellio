package com.constellio.app.modules.restapi.document.dao;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.exception.OptimisticLockException;
import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.exception.UnresolvableOptimisticLockException;
import com.constellio.app.modules.restapi.core.exception.UnsupportedMetadataTypeException;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.modules.restapi.document.dto.ContentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentContentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentTypeDto;
import com.constellio.app.modules.restapi.document.dto.ExtendedAttributeDto;
import com.constellio.app.modules.restapi.document.exception.DocumentContentNotFoundException;
import com.constellio.app.modules.restapi.document.exception.DocumentTypeNotFoundException;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentImplRuntimeException;
import com.constellio.model.services.contents.ContentManager.ContentVersionDataSummaryResponse;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentManagerRuntimeException;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.restapi.document.enumeration.VersionType.MAJOR;

public class DocumentDao extends BaseDao {

    private static final String LAST_VERSION = "last";

    public Record createDocument(User user, MetadataSchema documentSchema, DocumentDto document, Content content, String flush) throws Exception {
        Transaction transaction = buildTransaction(flush, user);

        Record documentRecord = recordServices.newRecordWithSchema(documentSchema);

        Record documentTypeRecord = getDocumentTypeRecord(document.getType(), documentSchema.getCollection());

        updateDocumentMetadataValues(documentRecord, documentTypeRecord, documentSchema, document, content, false);
        updateDocumentCustomMetadataValues(documentRecord, documentSchema, document.getExtendedAttributes(), false);

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
            documentTypeRecord = getDocumentTypeRecord(document.getType(), documentRecord.getCollection());
        }

        updateDocumentMetadataValues(documentRecord, documentTypeRecord, documentSchema, document, content, partial);
        updateDocumentCustomMetadataValues(documentRecord, documentSchema, document.getExtendedAttributes(), partial);

        transaction.add(documentRecord);

        try {
            recordServices.execute(transaction);
        } catch (RecordServicesException.UnresolvableOptimisticLockingConflict e) {
            throw new UnresolvableOptimisticLockException(document.getId());
        } catch (RecordServicesException.OptimisticLocking e) {
            throw new OptimisticLockException(document.getId(), document.getETag(), e.getVersion());
        }

        return documentRecord;
    }

    public DocumentContentDto getContent(Record documentRecord, String version) {
        try {
            Content content = getMetadataValue(documentRecord, Document.CONTENT);
            if (content == null) throw new DocumentContentNotFoundException(documentRecord.getId(), version);

            ContentVersion contentVersion = version.equals(LAST_VERSION) ?
                    content.getCurrentVersion():
                    content.getVersion(version);
            InputStream stream = contentManager.getContentInputStream(contentVersion.getHash(), contentVersion.getFilename());

            String mimeType = getMetadataValue(documentRecord, Document.MIME_TYPE);

            return DocumentContentDto.builder().content(stream).mimeType(mimeType).filename(contentVersion.getFilename()).build();
        } catch (RecordServicesRuntimeException.NoSuchRecordWithId |
                ContentImplRuntimeException.ContentImplRuntimeException_NoSuchVersion |
                ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent e ) {
            throw new DocumentContentNotFoundException(documentRecord.getId(), version);
        }
    }

    public List<ExtendedAttributeDto> getExtendedAttributes(MetadataSchema schema, Record record) {
        List<ExtendedAttributeDto> extendedAttributes = Lists.newArrayList();

        for (Metadata metadata : schema.getMetadatas().onlyUSR()) {
            List<String> values = Lists.newArrayList();
            for (Object value : record.getValues(metadata)) {
                if (metadata.getType() == MetadataValueType.DATE) {
                    values.add(value != null ? DateUtils.format((LocalDate) value, getDateFormat()) : null);
                } else if (metadata.getType() == MetadataValueType.DATE_TIME) {
                    values.add(value != null ? DateUtils.format((LocalDateTime) value, getDateTimeFormat()) : null);
                } else {
                    values.add(value != null ? String.valueOf(value) : null);
                }
            }

            extendedAttributes.add(ExtendedAttributeDto.builder().key(metadata.getLocalCode()).values(values).build());
        }
        return extendedAttributes;
    }

    public MetadataSchema getLinkedMetadataSchema(DocumentDto document, String collection) {
        Record documentTypeRecord = getDocumentTypeRecord(document.getType(), collection);
        return getDocumentMetadataSchema(documentTypeRecord, collection);
    }

    public void deleteContent(Content content) {
        contentManager.markForDeletionIfNotReferenced(content.getCurrentVersion().getHash());
    }

    public void deleteDocument(User user, Record documentRecord, boolean physical)  {
        Boolean logicallyDeleted = documentRecord.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS);

        if (physical) {
            if (!Boolean.TRUE.equals(logicallyDeleted)) {
                recordServices.logicallyDelete(documentRecord, user);
            }
            recordServices.physicallyDelete(documentRecord, user);
        } else {
            if (Boolean.TRUE.equals(logicallyDeleted)) throw new RecordLogicallyDeletedException(documentRecord.getId());
            recordServices.logicallyDelete(documentRecord, user);
        }
    }

    public Content uploadContent(User user, Record documentRecord, ContentDto documentContent, InputStream contentInputStream) {
        boolean isMajor = documentContent.getVersionType() == MAJOR;

        String filename = documentContent.getFilename();
        ContentVersionDataSummaryResponse contentResponse = contentManager.upload(contentInputStream,
                new UploadOptions(false, false, false, filename));

        Content content = documentRecord != null ? this.<Content>getMetadataValue(documentRecord, Document.CONTENT) : null;
        if (content != null) {
            if (filename == null) {
                return content.updateContent(user, contentResponse.getContentVersionDataSummary(), isMajor);
            }
            return content.updateContentWithName(user, contentResponse.getContentVersionDataSummary(), isMajor, filename);
        }

        if (filename == null) throw new RequiredParameterException("content.filename");

        if (isMajor) {
            return contentManager.createMajor(user, filename, contentResponse.getContentVersionDataSummary());
        }
        return contentManager.createMinor(user, filename, contentResponse.getContentVersionDataSummary());
    }

    private Record getDocumentTypeRecord(DocumentTypeDto type, String collection) {
        if (type == null) return null;

        Record record;
        if (!Strings.isNullOrEmpty(type.getId())) {
            record = getRecordById(type.getId());

            if (record == null) throw new DocumentTypeNotFoundException("id", type.getId());
        } else {
            MetadataSchema schema = getMetadataSchema(collection, DocumentType.SCHEMA_TYPE);
            Metadata metadata = getMetadata(schema, DocumentType.CODE);
            record = getRecordByMetadata(metadata, type.getCode());

            if (record == null) throw new DocumentTypeNotFoundException("code", type.getCode());
        }
        return record;
    }

    public MetadataSchema getDocumentMetadataSchema(Record documentTypeRecord, String collection) {
        if (documentTypeRecord != null) {
            String linkedSchemaCode = getMetadataValue(documentTypeRecord, DocumentType.LINKED_SCHEMA);
            return getMetadataSchema(collection, Document.SCHEMA_TYPE, linkedSchemaCode);
        }
        return getMetadataSchema(collection, Document.SCHEMA_TYPE);
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

    private <T> void updateDocumentMetadataValue(Record documentRecord, MetadataSchema schema, String metadataCode, T value) {
        updateDocumentMetadataValue(documentRecord, schema, metadataCode, value, false);
    }

    private <T> void updateDocumentMetadataValue(Record documentRecord, MetadataSchema schema, String metadataCode,
                                                 T value, boolean ignoreNull) {
        if (ignoreNull && value == null) return;
        updateMetadataValue(documentRecord, schema, metadataCode, value);
    }

    private void updateDocumentCustomMetadataValues(Record documentRecord, MetadataSchema schema,
                                                    List<ExtendedAttributeDto> attributes, boolean partial) {
        if (!partial || attributes != null) {
            clearCustomMetadataValues(documentRecord, schema);
        }

        for (ExtendedAttributeDto attribute : ListUtils.nullToEmpty(attributes)) {
            Metadata metadata = schema.getMetadata(attribute.getKey());

            List<Object> values = new ArrayList<>(attribute.getValues().size());
            for (String value : attribute.getValues()) {
                switch (metadata.getType()) {
                    case STRING: case TEXT: case REFERENCE: values.add(value); break;
                    case DATE: values.add(DateUtils.parseLocalDate(value, getDateFormat())); break;
                    case DATE_TIME: values.add(DateUtils.parseLocalDateTime(value, getDateTimeFormat())); break;
                    case NUMBER: values.add(Double.valueOf(value)); break;
                    case BOOLEAN: values.add(Boolean.valueOf(value)); break;
                    default: throw new UnsupportedMetadataTypeException(metadata.getType().name());
                }
            }
            documentRecord.set(metadata, metadata.isMultivalue() ? values : values.get(0));
        }
    }

}
