package com.constellio.app.modules.rm.servlet;

import com.constellio.app.api.pdf.pdfjs.services.PdfJSServices;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.services.menu.behaviors.DocumentMenuItemActionBehaviors;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.SignatureExternalAccessUrl;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ExternalAccessUser;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.emails.EmailServicesException.CannotSendEmailException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.security.roles.Roles;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.UUID;

public class SignatureExternalAccessDao {
	public static final String UNAUTHORIZED = "Unauthorized";
	public static final String MISSING_DOCUMENT_PARAM = "Missing document param.";
	public static final String INVALID_DOCUMENT_PARAM = "The document does not exist.";
	public static final String MISSING_EXTERNAL_USER_FULLNAME_PARAM = "Missing external user fullname param.";
	public static final String MISSING_EXTERNAL_USER_EMAIL_PARAM = "Missing external user email param.";
	public static final String MISSING_DATE_PARAM = "Missing expiration date param.";
	public static final String INVALID_DATE_PARAM = "The expiration date is not valid.";
	public static final String MISSING_LANGUAGE_PARAM = "Missing language param.";
	public static final String ACTION_IMPOSSIBLE = "The url generation is not possible for this type of document.";
	public static final String CANNOT_SEND_EMAIL = "Unable to send email.";
	private static final String CANNOT_SAVE_RECORD = "Impossible to save record.";

	private AppLayerFactory appLayerFactory;

	private RecordServices recordServices;
	private UserServices userServices;
	private PdfJSServices pdfJSServices;
	private MetadataSchemasManager metadataSchemasManager;
	private RolesManager rolesManager;
	private RMSchemasRecordsServices rm;
	private DocumentRecordActionsServices documentRecordActionsServices;
	private DocumentMenuItemActionBehaviors documentMenuItemActionBehaviors;

	public SignatureExternalAccessDao(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;

		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		pdfJSServices = new PdfJSServices(appLayerFactory);
		metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();
	}

	private void initWithCollection(String collection) {
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		documentRecordActionsServices = new DocumentRecordActionsServices(collection, appLayerFactory);
		documentMenuItemActionBehaviors = new DocumentMenuItemActionBehaviors(collection, appLayerFactory);
	}

	public String accessExternalSignature(String accessId, String token, String language)
			throws SignatureExternalAccessServiceException {

		if (StringUtils.isBlank(accessId)) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		Record accessRecord;
		try {
			accessRecord = recordServices.getDocumentById(accessId);
		} catch (Exception e) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		if (accessRecord == null) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		initWithCollection(accessRecord.getCollection());

		SignatureExternalAccessUrl signatureAccess;
		try {
			signatureAccess = rm.wrapSignatureExternalAccessUrl(accessRecord);
		} catch (Exception e) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		if (signatureAccess == null) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		if (StringUtils.isBlank(token)) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		if (!signatureAccess.getToken().equals(token)) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		if (signatureAccess.getStatus() != ExternalAccessUrlStatus.OPEN &&
			signatureAccess.getStatus() != ExternalAccessUrlStatus.TO_CLOSE) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		if (signatureAccess.getExpirationDate().isBefore(LocalDate.now())) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		if (StringUtils.isBlank(language)) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		Record recordToAccess = recordServices.getDocumentById(signatureAccess.getAccessRecord());
		Metadata metadata = metadataSchemasManager.getSchemaOf(recordToAccess).get(Document.CONTENT);
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(recordToAccess.getCollection());
		MetadataSchema userSchema = types.getDefaultSchema(User.SCHEMA_TYPE);
		Record tempUserRecord = recordServices.newRecordWithSchema(userSchema, UUID.randomUUID().toString());
		Roles roles = rolesManager.getCollectionRoles(recordToAccess.getCollection());
		ExternalAccessUser user = new ExternalAccessUser(tempUserRecord, types, roles, signatureAccess);
		Locale locale = new Locale(language);
		return pdfJSServices.getExternalViewerUrl(recordToAccess, metadata, user, locale, null, null, true);
	}

	public String createExternalSignatureUrl(String username, String documentId, String externalUserFullname,
											 String externalUserEmail, String expirationDate, String language)
			throws SignatureExternalAccessServiceException {

		if (StringUtils.isBlank(documentId)) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_BAD_REQUEST, MISSING_DOCUMENT_PARAM);
		}

		Record documentRecord;
		try {
			documentRecord = recordServices.getDocumentById(documentId);
		} catch (Exception e) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_BAD_REQUEST, INVALID_DOCUMENT_PARAM);
		}

		if (documentRecord == null) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_BAD_REQUEST, INVALID_DOCUMENT_PARAM);
		}

		initWithCollection(documentRecord.getCollection());

		User user = userServices.getUserInCollection(username, documentRecord.getCollection());
		if (!user.hasWriteAccess().on(documentRecord) ||
			!user.has(RMPermissionsTo.GENERATE_EXTERNAL_SIGNATURE_URL).globally()) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		Document document;
		try {
			document = rm.wrapDocument(documentRecord);
		} catch (Exception e) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_BAD_REQUEST, INVALID_DOCUMENT_PARAM);
		}

		if (document == null) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_BAD_REQUEST, INVALID_DOCUMENT_PARAM);
		}

		if (!documentRecordActionsServices.isGenerateExternalSignatureUrlActionPossible(documentRecord, user)) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_BAD_REQUEST, ACTION_IMPOSSIBLE);
		}

		if (StringUtils.isBlank(externalUserFullname)) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_BAD_REQUEST, MISSING_EXTERNAL_USER_FULLNAME_PARAM);
		}

		// TODO --> Enable this validation when Teams will be ready to send external user email.
		/*if (StringUtils.isBlank(externalUserEmail)) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_BAD_REQUEST, MISSING_EXTERNAL_USER_EMAIL_PARAM);
		}*/

		if (StringUtils.isBlank(expirationDate)) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_BAD_REQUEST, MISSING_DATE_PARAM);
		}

		LocalDate convertedDate;
		try {
			convertedDate = new LocalDate(expirationDate);
		} catch (Exception e) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_BAD_REQUEST, INVALID_DATE_PARAM);
		}

		if (StringUtils.isBlank(language)) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_BAD_REQUEST, MISSING_LANGUAGE_PARAM);
		}

		try {
			return documentMenuItemActionBehaviors.createExternalSignatureUrl(documentId, externalUserFullname,
					externalUserEmail, convertedDate, language, user);
		} catch (RecordServicesException e) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_BAD_REQUEST, CANNOT_SAVE_RECORD);
		} catch (CannotSendEmailException e) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_BAD_REQUEST, CANNOT_SEND_EMAIL);
		}
	}
}
