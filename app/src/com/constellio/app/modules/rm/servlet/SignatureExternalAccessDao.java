package com.constellio.app.modules.rm.servlet;

import com.constellio.app.api.pdf.pdfjs.services.PdfJSServices;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.ExpiredSignedUrlException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.SignatureExternalAccessServices;
import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.servlet.SignatureExternalAccessServiceException.SignatureExternalAccessServiceException_CannotCreateAccess;
import com.constellio.app.modules.rm.servlet.SignatureExternalAccessServiceException.SignatureExternalAccessServiceException_CannotSendEmail;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.SignatureExternalAccessUrl;
import com.constellio.app.servlet.BaseServletDao;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ExternalAccessUser;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.emails.EmailServicesException.CannotSendEmailException;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.roles.Roles;
import com.constellio.model.services.security.roles.RolesManager;
import org.joda.time.LocalDate;

import java.util.Locale;
import java.util.UUID;

import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_DOCUMENT;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_EXPIRATION_DATE;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_ID;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_INTERNAL_USER;

public class SignatureExternalAccessDao extends BaseServletDao {

	private PdfJSServices pdfJSServices;
	private RolesManager rolesManager;
	private RMSchemasRecordsServices rm;
	private SignatureExternalAccessServices signatureServices;
	private DocumentRecordActionsServices documentRecordActionsServices;

	public SignatureExternalAccessDao() {
		pdfJSServices = new PdfJSServices(appLayerFactory);
		rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();
	}

	private void initWithCollection(String collection) {
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		signatureServices = new SignatureExternalAccessServices(collection, appLayerFactory);
		documentRecordActionsServices = new DocumentRecordActionsServices(collection, appLayerFactory);
	}

	public String accessExternalSignature(Record accessRecord, String token, String language,
										  String ipAddress) {
		initWithCollection(accessRecord.getCollection());

		SignatureExternalAccessUrl signatureAccess;
		try {
			signatureAccess = rm.wrapSignatureExternalAccessUrl(accessRecord);
		} catch (Exception e) {
			throw new InvalidParameterException(PARAM_ID, accessRecord.getId());
		}

		if (signatureAccess == null) {
			throw new InvalidParameterException(PARAM_ID, accessRecord.getId());
		}

		if (!signatureAccess.getToken().equals(token)) {
			throw new UnauthorizedAccessException();
		}

		if (signatureAccess.getStatus() != ExternalAccessUrlStatus.OPEN &&
			signatureAccess.getStatus() != ExternalAccessUrlStatus.TO_CLOSE) {
			throw new ExpiredSignedUrlException();
		}

		if (signatureAccess.getExpirationDate().isBefore(LocalDate.now())) {
			throw new ExpiredSignedUrlException();
		}

		Record recordToAccess = recordServices.getDocumentById(signatureAccess.getAccessRecord());
		Metadata metadata = metadataSchemasManager.getSchemaOf(recordToAccess).get(Document.CONTENT);
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(recordToAccess.getCollection());
		MetadataSchema userSchema = types.getDefaultSchema(User.SCHEMA_TYPE);
		Record tempUserRecord = recordServices.newRecordWithSchema(userSchema, UUID.randomUUID().toString());
		Roles roles = rolesManager.getCollectionRoles(recordToAccess.getCollection());
		ExternalAccessUser user = new ExternalAccessUser(tempUserRecord, types, roles, signatureAccess, ipAddress);
		Locale locale = new Locale(language);
		return pdfJSServices.getExternalViewerUrl(recordToAccess, metadata, user, locale, null, null, true);
	}

	public void sendSignatureRequest(User requester, Record documentRecord, Record internalUserRecord,
									 String externalUserFullname, String externalUserEmail, String expirationDate,
									 String language) {

		initWithCollection(documentRecord.getCollection());

		Document document;
		try {
			document = rm.wrapDocument(documentRecord);
		} catch (Exception e) {
			throw new InvalidParameterException(PARAM_DOCUMENT, documentRecord.getId());
		}

		if (document == null) {
			throw new InvalidParameterException(PARAM_DOCUMENT, documentRecord.getId());
		}

		if (!documentRecordActionsServices.isSendSignatureRequestActionPossible(documentRecord, requester)) {
			throw new InvalidParameterException(PARAM_DOCUMENT, documentRecord.getId());
		}

		LocalDate convertedDate;
		try {
			convertedDate = new LocalDate(expirationDate);
		} catch (Exception e) {
			throw new InvalidParameterException(PARAM_EXPIRATION_DATE, expirationDate);
		}

		try {
			if (internalUserRecord == null) {
				signatureServices.sendSignatureRequest(document.getId(), null, externalUserFullname,
						externalUserEmail, convertedDate, language, requester);
			} else {
				User internalUser;
				try {
					internalUser = rm.wrapUser(internalUserRecord);
				} catch (Exception e) {
					throw new InvalidParameterException(PARAM_INTERNAL_USER, internalUserRecord.getId());
				}

				if (internalUser == null) {
					throw new InvalidParameterException(PARAM_INTERNAL_USER, internalUserRecord.getId());
				}

				signatureServices.sendSignatureRequest(document.getId(), internalUser.getId(), internalUser.getTitle(),
						internalUser.getEmail(), convertedDate, language, requester);
			}
		} catch (RecordServicesException e) {
			throw new SignatureExternalAccessServiceException_CannotCreateAccess();
		} catch (CannotSendEmailException e) {
			throw new SignatureExternalAccessServiceException_CannotSendEmail();
		}
	}
}
