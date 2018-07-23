package com.constellio.app.modules.restapi.document;

import com.constellio.app.modules.restapi.ace.AceService;
import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.exception.InvalidMetadataValueException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotFoundException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotMultivalueException;
import com.constellio.app.modules.restapi.core.exception.MetadataReferenceNotAllowedException;
import com.constellio.app.modules.restapi.core.exception.UnsupportedMetadataTypeException;
import com.constellio.app.modules.restapi.core.service.BaseService;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.core.util.StringUtils;
import com.constellio.app.modules.restapi.document.dao.DocumentDao;
import com.constellio.app.modules.restapi.document.dto.AceListDto;
import com.constellio.app.modules.restapi.document.dto.ContentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentContentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentTypeDto;
import com.constellio.app.modules.restapi.document.dto.ExtendedAttributeDto;
import com.constellio.app.modules.restapi.validation.ValidationService;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.google.common.base.Strings;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static com.constellio.app.modules.restapi.core.util.ListUtils.isNullOrEmpty;
import static com.constellio.app.modules.restapi.core.util.ListUtils.nullToEmpty;
import static com.constellio.app.modules.restapi.document.enumeration.VersionType.MAJOR;
import static com.constellio.app.modules.restapi.document.enumeration.VersionType.MINOR;

public class DocumentService extends BaseService {

    @Inject
    private DocumentDao documentDao;

    @Inject
    private AceService aceService;
    @Inject
    private ValidationService validationService;

    private static String SCHEMA_TYPE = SchemaTypes.DOCUMENT.name();

    public DocumentDto create(String host, String folderId, String serviceKey, String method, String date, int expiration, String signature,
                              DocumentDto document, InputStream contentInputStream, String flushMode, Set<String> filters) throws Exception {
        validateParameters(host, folderId, serviceKey, SCHEMA_TYPE, method, date, expiration, null, null, signature);

        Record folder = getRecord(folderId, true);
        User user = getUser(serviceKey, folder.getCollection());
        validationService.validateUserAccess(user, folder, method);

        MetadataSchema documentSchema = documentDao.getLinkedMetadataSchema(document, folder.getCollection());
        validateExtendedAttributes(document.getExtendedAttributes(), documentSchema);

        validationService.validateAuthorizations(document.getDirectAces(), folder.getCollection());

        Content content = null;
        if (document.getContent() != null) {
            content = documentDao.uploadContent(user, null, document.getContent(), contentInputStream);
        }

        try {
            boolean acesModified = false;
            Record createdDocumentRecord = documentDao.createDocument(user, documentSchema, document, content, flushMode);
            if (!isNullOrEmpty(document.getDirectAces())) {
                aceService.addAces(user, createdDocumentRecord, document.getDirectAces());
                acesModified = true;
            }
            return adapt(document, createdDocumentRecord, documentSchema, acesModified, filters);
        } catch (Exception e) {
            if (content != null) documentDao.deleteContent(content);
            throw e;
        }
    }

    public void delete(String host, String id, String serviceKey, String method, String date, int expiration, Boolean physical,
                       String signature) throws Exception {
        validateParameters(host, id, serviceKey, SCHEMA_TYPE, method, date, expiration, null, physical, signature);

        Record document = getRecord(id, false);
        User user = getUser(serviceKey, document.getCollection());
        validationService.validateUserAccess(user, document, method);

        documentDao.deleteDocument(user, document, Boolean.TRUE.equals(physical));
    }

    public DocumentContentDto getContent(String host, String id, String serviceKey, String method, String date, int expiration,
                                         String version, String signature) throws Exception {
        validateParameters(host, id, serviceKey, SCHEMA_TYPE, method, date, expiration, version, null, signature);

        Record document = getRecord(id, false);
        User user = getUser(serviceKey, document.getCollection());
        validationService.validateUserAccess(user, document, method);

        return documentDao.getContent(document, version);
    }

    public DocumentDto get(String host, String id, String serviceKey, String method, String date, int expiration, String signature,
                           Set<String> filters) throws Exception {
        validateParameters(host, id, serviceKey, SCHEMA_TYPE, method, date, expiration,null, null, signature);

        Record document = getRecord(id, false);
        User user = getUser(serviceKey, document.getCollection());
        validationService.validateUserAccess(user, document, method);

        MetadataSchema documentSchema = documentDao.getMetadataSchema(document);

        return adapt(null, document, documentSchema, false, filters);
    }

    public DocumentDto update(String host, String id, String serviceKey, String method, String date, int expiration, String signature,
                              DocumentDto document, InputStream contentInputStream, boolean partial, String flushMode,
                              Set<String> filters) throws Exception {
        validateParameters(host, id, serviceKey, SCHEMA_TYPE, method, date, expiration, null, null, signature);

        Record documentRecord = getRecord(id, true);
        if (document.getETag() != null) {
            validationService.validateETag(id, document.getETag(), documentRecord.getVersion());
        }

        User user = getUser(serviceKey, documentRecord.getCollection());
        validationService.validateUserAccess(user, documentRecord, method);

        // make sure that folderId is valid
        if (!partial || document.getFolderId() != null) {
            getRecord(document.getFolderId(), true);
        }

        MetadataSchema documentSchema;
        if (partial && document.getType() == null) {
            String documentTypeId = documentDao.getMetadataValue(documentRecord, Document.TYPE);
            Record documentTypeRecord = documentTypeId != null ? getRecord(documentTypeId, true) : null;
            documentSchema = documentDao.getDocumentMetadataSchema(documentTypeRecord, documentRecord.getCollection());
        } else {
            documentSchema = documentDao.getLinkedMetadataSchema(document, documentRecord.getCollection());
        }

        validateExtendedAttributes(document.getExtendedAttributes(), documentSchema);
        validationService.validateAuthorizations(document.getDirectAces(), documentRecord.getCollection());

        Content content = null;
        if (document.getContent() != null) {
            content = documentDao.uploadContent(user, documentRecord, document.getContent(), contentInputStream);
        }

        try {
            boolean acesModified = false;
            documentRecord = documentDao.updateDocument(user, documentRecord, documentSchema, document, content, partial, flushMode);
            if (!partial || document.getDirectAces() != null) {
                acesModified = aceService.updateAces(user, documentRecord, nullToEmpty(document.getDirectAces()));
            }

            return adapt(document, documentRecord, documentSchema, acesModified, filters);
        } catch (Exception e) {
            if (content != null) documentDao.deleteContent(content);
            throw e;
        }
    }

    private DocumentDto adapt(DocumentDto document, Record documentRecord, MetadataSchema documentSchema, boolean acesModified, Set<String> filters) {
        if (document == null) document = DocumentDto.builder().build();

        if (!acesModified) {
            document.setETag(String.valueOf(documentRecord.getVersion()));
        }

        document.setId(documentRecord.getId());
        document.setTitle(!filters.contains("title") ? documentRecord.getTitle() : null);
        document.setFolderId(!filters.contains("folderId") ? this.<String>getMetadataValue(documentRecord, Document.FOLDER) : null);
        document.setKeywords(!filters.contains("keywords") ? this.<List<String>>getMetadataValue(documentRecord, Document.KEYWORDS) : null);
        document.setAuthor(!filters.contains("author") ? this.<String>getMetadataValue(documentRecord, Document.AUTHOR) : null);
        document.setOrganization(!filters.contains("organization") ? this.<String>getMetadataValue(documentRecord, Document.COMPANY) : null);
        document.setSubject(!filters.contains("subject") ? this.<String>getMetadataValue(documentRecord, Document.SUBJECT) : null);

        if (!filters.contains("type")) {
            String documentTypeId = getMetadataValue(documentRecord, Document.TYPE);
            if (!Strings.isNullOrEmpty(documentTypeId)) {
                Record documentTypeRecord = getRecord(documentTypeId, false);

                document.setType(documentTypeRecord == null ? null :
                        DocumentTypeDto.builder()
                                .id(documentTypeRecord.getId())
                                .code(this.<String>getMetadataValue(documentTypeRecord, DocumentType.CODE))
                                .title(documentTypeRecord.getTitle())
                                .build());
            }
        } else {
            document.setType(null);
        }

        if (!filters.contains("content")) {
            Content content = getMetadataValue(documentRecord, Document.CONTENT);
            if (content != null) {
                document.setContent(ContentDto.builder()
                        .filename(content.getCurrentVersion().getFilename())
                        .versionType(content.getCurrentVersion().isMajor() ? MAJOR : MINOR)
                        .version(content.getCurrentVersion().getVersion())
                        .hash(content.getCurrentVersion().getHash())
                        .build());
            }
        } else {
            document.setContent(null);
        }

        if (filters.contains("directAces") && filters.contains("indirectAces")) {
            document.setDirectAces(null);
            document.setInheritedAces(null);
        } else {
            AceListDto aces = aceService.getAces(documentRecord);
            document.setDirectAces(filters.contains("directAces") ? null : aces.getDirectAces());
            document.setInheritedAces(filters.contains("inheritedAces") ? null : aces.getInheritedAces());
        }

        if (filters.contains("extendedAttributes")) {
            document.setExtendedAttributes(null);
        } else if (document.getExtendedAttributes() == null) {
                document.setExtendedAttributes(documentDao.getExtendedAttributes(documentSchema, documentRecord));
        }

        return document;
    }

    private void validateParameters(String host, String id, String serviceKey, String schemaType, String method, String date, int expiration,
                                    String version, Boolean physical, String signature) throws Exception {
        validationService.validateHost(host);
        validationService.validateUrl(date, expiration);
        validationService.validateSignature(host, id, serviceKey, schemaType, method, date, expiration, version, physical, signature);
    }

    private void validateExtendedAttributes(List<ExtendedAttributeDto> extendedAttributes, MetadataSchema schema) {
        if (ListUtils.isNullOrEmpty(extendedAttributes)) return;

        String dateFormat = documentDao.getDateFormat();
        String dateTimeFormat = documentDao.getDateTimeFormat();

        for (ExtendedAttributeDto attribute : extendedAttributes) {
            Metadata metadata;
            try {
                metadata = schema.getMetadata(attribute.getKey());
            } catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
                throw new MetadataNotFoundException(attribute.getKey());
            }

            if (!metadata.isMultivalue() && attribute.getValues().size() != 1) {
                throw new MetadataNotMultivalueException(attribute.getKey());
            }

            for (String value : attribute.getValues()) {
                switch (metadata.getType()) {
                    case REFERENCE:
                        Record record = getRecord(value, true);
                        if (!metadata.getAllowedReferences().getAllowedSchemaType().equals(record.getTypeCode())) {
                            throw new MetadataReferenceNotAllowedException(record.getTypeCode(), attribute.getKey());
                        }
                        break;
                    case DATE: DateUtils.validateLocalDate(value, dateFormat); break;
                    case DATE_TIME: DateUtils.validateLocalDateTime(value, dateTimeFormat); break;
                    case NUMBER:
                        if (!StringUtils.isUnsignedDouble(value)) {
                            throw new InvalidMetadataValueException(metadata.getType().name(), value);
                        }
                        break;
                    case BOOLEAN:
                        if (!value.equals("true") && !value.equals("false")) {
                            throw new InvalidMetadataValueException(metadata.getType().name(), value);
                        }
                        break;
                    case STRING: case TEXT: break;
                    default: throw new UnsupportedMetadataTypeException(metadata.getType().name());
                }
            }
        }
    }

    @Override
    protected BaseDao getDao() {
        return documentDao;
    }
}
