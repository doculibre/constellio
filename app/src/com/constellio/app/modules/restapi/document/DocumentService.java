package com.constellio.app.modules.restapi.document;

import com.constellio.app.modules.restapi.ace.AceService;
import com.constellio.app.modules.restapi.core.adaptor.ResourceAdaptor;
import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.exception.InvalidMetadataValueException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotFoundException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotMultivalueException;
import com.constellio.app.modules.restapi.core.exception.MetadataReferenceNotAllowedException;
import com.constellio.app.modules.restapi.core.exception.UnsupportedMetadataTypeException;
import com.constellio.app.modules.restapi.core.service.ResourceService;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.core.util.StringUtils;
import com.constellio.app.modules.restapi.document.adaptor.DocumentAdaptor;
import com.constellio.app.modules.restapi.document.dao.DocumentDao;
import com.constellio.app.modules.restapi.document.dto.DocumentContentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentDto;
import com.constellio.app.modules.restapi.document.dto.ExtendedAttributeDto;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static com.constellio.app.modules.restapi.core.util.ListUtils.isNullOrEmpty;
import static com.constellio.app.modules.restapi.core.util.ListUtils.nullToEmpty;

public class DocumentService extends ResourceService {

	@Inject
	private DocumentDao documentDao;
	@Inject
	private DocumentAdaptor documentAdaptor;
	@Inject
	private AceService aceService;

	public DocumentDto create(String host, String folderId, String serviceKey, String method, String date,
							  int expiration, String signature, DocumentDto document, InputStream contentInputStream,
							  String flushMode, Set<String> filters) throws Exception {
		validateParameters(host, folderId, serviceKey, method, date, expiration, null, null, signature);

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
			return getAdaptor().adapt(document, createdDocumentRecord, documentSchema, acesModified, filters);
		} catch (Exception e) {
			if (content != null) {
				documentDao.deleteContent(content);
			}
			throw e;
		}
	}

	public void delete(String host, String id, String serviceKey, String method, String date, int expiration,
					   Boolean physical, String signature) throws Exception {
		validateParameters(host, id, serviceKey, method, date, expiration, null, physical, signature);

		Record document = getRecord(id, false);
		User user = getUser(serviceKey, document.getCollection());
		validationService.validateUserAccess(user, document, method);

		documentDao.deleteDocument(user, document, Boolean.TRUE.equals(physical));
	}

	public DocumentContentDto getContent(String host, String id, String serviceKey, String method, String date,
										 int expiration, String version, String signature) throws Exception {
		validateParameters(host, id, serviceKey, method, date, expiration, version, null, signature);

		Record document = getRecord(id, false);
		User user = getUser(serviceKey, document.getCollection());
		validationService.validateUserAccess(user, document, method);

		return documentDao.getContent(document, version);
	}

	public DocumentDto get(String host, String id, String serviceKey, String method, String date, int expiration,
						   String signature, Set<String> filters) throws Exception {
		return getResource(host, id, serviceKey, method, date, expiration, signature, filters);
	}

	public DocumentDto update(String host, String id, String serviceKey, String method, String date, int expiration,
							  String signature, DocumentDto document, InputStream contentInputStream, boolean partial,
							  String flushMode, Set<String> filters) throws Exception {
		validateParameters(host, id, serviceKey, method, date, expiration, null, null, signature);

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

			return getAdaptor().adapt(document, documentRecord, documentSchema, acesModified, filters);
		} catch (Exception e) {
			if (content != null) {
				documentDao.deleteContent(content);
			}
			throw e;
		}
	}

	private void validateExtendedAttributes(List<ExtendedAttributeDto> extendedAttributes, MetadataSchema schema) {
		if (ListUtils.isNullOrEmpty(extendedAttributes)) {
			return;
		}

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
					case DATE:
						DateUtils.validateLocalDate(value, dateFormat);
						break;
					case DATE_TIME:
						DateUtils.validateLocalDateTime(value, dateTimeFormat);
						break;
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
					case STRING:
					case TEXT:
						break;
					default:
						throw new UnsupportedMetadataTypeException(metadata.getType().name());
				}
			}
		}
	}

	@Override
	protected BaseDao getDao() {
		return documentDao;
	}

	@Override
	protected String getSchemaType() {
		return SchemaTypes.DOCUMENT.name();
	}

	@Override
	protected ResourceAdaptor<DocumentDto> getAdaptor() {
		return documentAdaptor;
	}
}
