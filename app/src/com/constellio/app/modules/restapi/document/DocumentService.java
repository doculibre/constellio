package com.constellio.app.modules.restapi.document;

import com.constellio.app.modules.restapi.ace.AceService;
import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.document.adaptor.DocumentAdaptor;
import com.constellio.app.modules.restapi.document.dao.DocumentDao;
import com.constellio.app.modules.restapi.document.dto.DocumentContentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentDto;
import com.constellio.app.modules.restapi.resource.adaptor.ResourceAdaptor;
import com.constellio.app.modules.restapi.resource.service.ResourceService;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;

import javax.inject.Inject;
import java.io.InputStream;
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
		String collection = folder.getCollection();
		User user = getUser(serviceKey, collection);
		validateUserAccess(user, folder, method);

		MetadataSchema documentSchema = documentDao.getLinkedMetadataSchema(document.getType(), collection);
		validateExtendedAttributes(document.getExtendedAttributes(), documentSchema);

		validateAuthorizations(document.getDirectAces(), collection);

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
		validateUserAccess(user, document, method);

		documentDao.deleteDocument(user, document, Boolean.TRUE.equals(physical));
	}

	public DocumentContentDto getContent(String host, String id, String serviceKey, String method, String date,
										 int expiration, String version, String signature) throws Exception {
		validateParameters(host, id, serviceKey, method, date, expiration, version, null, signature);

		Record document = getRecord(id, false);
		User user = getUser(serviceKey, document.getCollection());
		validateUserAccess(user, document, method);

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
			validateETag(id, document.getETag(), documentRecord.getVersion());
		}

		User user = getUser(serviceKey, documentRecord.getCollection());
		validateUserAccess(user, documentRecord, method);

		// make sure that folderId is valid
		if (!partial || document.getFolderId() != null) {
			getRecord(document.getFolderId(), true);
		}

		MetadataSchema documentSchema;
		if (partial && document.getType() == null) {
			String documentTypeId = documentDao.getMetadataValue(documentRecord, Document.TYPE);
			Record documentTypeRecord = documentTypeId != null ? getRecord(documentTypeId, true) : null;
			documentSchema = documentDao.getResourceMetadataSchema(documentTypeRecord, documentRecord.getCollection());
		} else {
			documentSchema = documentDao.getLinkedMetadataSchema(document.getType(), documentRecord.getCollection());
		}

		validateExtendedAttributes(document.getExtendedAttributes(), documentSchema);
		validateAuthorizations(document.getDirectAces(), documentRecord.getCollection());

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

	@Override
	protected BaseDao getDao() {
		return documentDao;
	}

	@Override
	protected SchemaTypes getSchemaType() {
		return SchemaTypes.DOCUMENT;
	}

	@Override
	protected ResourceAdaptor<DocumentDto> getAdaptor() {
		return documentAdaptor;
	}
}
