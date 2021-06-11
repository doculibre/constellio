package com.constellio.app.modules.rm.services;

import com.constellio.app.api.pdf.signature.config.ESignatureConfigs;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.ExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.ExternalAccessUser;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.emails.EmailServicesException.CannotSendEmailException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class SignatureExternalAccessServices {

	private AppLayerFactory appLayerFactory;
	private String collection;

	private ModelLayerFactory modelLayerFactory;
	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;
	private SystemConfigurationsManager configsManager;
	private LoggingServices loggingServices;
	private SearchServices searchServices;
	private UserServices userServices;
	private Logger LOGGER = LoggerFactory.getLogger(SignatureExternalAccessServices.class);

	public SignatureExternalAccessServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		recordServices = modelLayerFactory.newRecordServices();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		configsManager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();
		loggingServices = modelLayerFactory.newLoggingServices();
		searchServices = modelLayerFactory.newSearchServices();
		userServices = modelLayerFactory.newUserServices();
	}

	public void sendSignatureNotifications(Record document, User requester, String localeCode) {
		if (requester instanceof ExternalAccessUser) {
			ExternalAccessUrl externalAccess = ((ExternalAccessUser) requester).getExternalAccessUrl();
			if (externalAccess != null && externalAccess.getUser() != null) {
				User internalUser = rm.getUser(externalAccess.getUser());
				if (internalUser != null) {
					requester = internalUser;
				}
			}
		}

		loggingServices.logSignedRecord(document, requester, LocalDateTime.now());

		LogicalSearchCondition typeCondition = from(rm.eventSchemaType())
				.where(rm.event.type()).isEqualTo(EventType.SIGN_DOCUMENT);

		LogicalSearchCondition otherTypeCondition = from(rm.eventSchemaType())
				.where(rm.event.type()).isEqualTo(EventType.SIGNATURE_REQUEST);

		LogicalSearchCondition condition = from(rm.eventSchemaType())
				.whereAnyCondition(typeCondition, otherTypeCondition)
				.andWhere(rm.event.recordIdentifier()).isEqualTo(document.getId());

		List<Record> records = searchServices.search(new LogicalSearchQuery(condition));
		List<Event> events = rm.wrapEvents(records);

		boolean hasRequest = false;
		for (Event event : events) {
			if (event.getType().equals(EventType.SIGNATURE_REQUEST)) {
				hasRequest = true;
				break;
			}
		}
		if (!hasRequest) {
			return;
		}

		Set<String> usernames = new HashSet<>();
		usernames.add(requester.getUsername());
		for (Event event : events) {
			usernames.add(event.getUsername());
		}

		for (String username : usernames) {
			String internalUserId = null;
			String name;
			String email;
			try {
				User internalUser = userServices.getUserInCollection(username, document.getCollection());
				internalUserId = internalUser.getId();
				name = internalUser.getTitle();
				email = internalUser.getEmail();
			} catch (Exception ignored) {
				// The user is an external user
				name = getExternalUserName(username);
				email = getExternalUserEmail(username);
			}

			try {
				sendSignatureNotification(document.getId(), internalUserId, name, email, localeCode, requester);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void sendSignatureNotification(String documentId, String internalUserId, String userName, String userMail,
										   String language, User requester)
			throws RecordServicesException, CannotSendEmailException {
		ESignatureConfigs eSignatureConfigs = new ESignatureConfigs(modelLayerFactory.getSystemConfigurationsManager());
		int duration = eSignatureConfigs.getSignedDocumentConsultationDurationInDays();
		LocalDate expirationDate = LocalDate.now().plusDays(duration);
		sendNotification(documentId, internalUserId, userName, userMail, expirationDate, language, requester,
				false);
	}

	public void sendSignatureRequest(String documentId, String internalUserId, String userName, String userMail,
									 LocalDate expirationDate, String language, User requester)
			throws RecordServicesException, CannotSendEmailException {
		Record record = rm.get(documentId);
		loggingServices.logSignatureRequest(record, requester, LocalDateTime.now());

		sendNotification(documentId, internalUserId, userName, userMail, expirationDate, language, requester,
				true);
	}

	private void sendNotification(String documentId, String internalUserId, String userName, String userMail,
								  LocalDate expirationDate, String language, User requester, boolean isRequest)
			throws RecordServicesException, CannotSendEmailException {

		if (StringUtils.isBlank(userMail)) {
			throw new CannotSendEmailException();
		}

		List<String> roles = new ArrayList<>();
		roles.add(Role.READ);
		if (isRequest) {
			roles.add(Role.WRITE);
		}

		ExternalAccessUrl accessUrl = rm.newSignatureExternalAccessUrl()
				.setToken(UUID.randomUUID().toString())
				.setUser(internalUserId)
				.setAccessRecord(documentId)
				.setRoles(roles)
				.setStatus(ExternalAccessUrlStatus.OPEN)
				.setFullname(userName)
				.setEmail(userMail)
				.setExpirationDate(expirationDate);

		if (isRequest) {
			accessUrl.setCreatedBy(requester.getId());
		}
		accessUrl.setCreatedOn(new LocalDateTime());

		Transaction transaction = new Transaction();
		transaction.add(accessUrl);

		recordServices.execute(transaction);

		String consultationLink = getUrlFromExternalAccess(accessUrl, language);
		sendMail(requester, documentId, accessUrl.getFullname(), accessUrl.getEmail(), expirationDate, consultationLink,
				isRequest, 1);
	}

	private void sendMail(User sender, String documentId, String receiverName, String receiverMail,
						  LocalDate expirationDate, String link, boolean isRequest, int attempt)
			throws CannotSendEmailException {
		try {
			EmailToSend emailToSend = newEmailToSend();

			EmailAddress userAddress = new EmailAddress(receiverName, receiverMail);
			emailToSend.setTemplate(RMEmailTemplateConstants.SIGNATURE_REQUEST);
			emailToSend.setTo(Arrays.asList(userAddress));
			emailToSend.setSendOn(TimeProvider.getLocalDateTime());
			String subjectId = isRequest ? "DocumentMenuItemActionBehaviors.signatureRequest"
										 : "DocumentMenuItemActionBehaviors.signatureNotification";
			emailToSend.setSubject($(subjectId));
			emailToSend.setParameters(buildMailParameters(sender, documentId, expirationDate, receiverName, link, isRequest));

			recordServices.add(emailToSend);
		} catch (RecordServicesException e) {
			if (attempt > 3) {
				throw new CannotSendEmailException();
			} else {
				LOGGER.warn("Attempt #" + attempt + " to send email failed, retrying...", e);
				sendMail(sender, documentId, receiverName, receiverMail, expirationDate, link, isRequest, attempt + 1);
			}
		}
	}

	private List<String> buildMailParameters(User sender, String documentId, LocalDate expirationDate,
											 String receiverName, String link, boolean isRequest) {
		List<String> params = new ArrayList<>();

		Document document = rm.getDocument(documentId);
		String docText = " \"" + document.getTitle() + "\" (" + document.getId() + ")";

		String dateFormat = modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.DATE_FORMAT);
		String expiration = expirationDate.toString(dateFormat);

		String titleId = isRequest ? "DocumentMenuItemActionBehaviors.signatureRequest"
								   : "DocumentMenuItemActionBehaviors.signatureNotification";
		String messageId = isRequest ? "DocumentMenuItemActionBehaviors.signatureRequestMessage"
									 : "DocumentMenuItemActionBehaviors.signatureNotificationMessage";
		String linkId = isRequest ? "DocumentMenuItemActionBehaviors.signatureRequestLinkMessage"
								  : "DocumentMenuItemActionBehaviors.signatureNotificationLinkMessage";

		params.add("title" + EmailToSend.PARAMETER_SEPARATOR + $(titleId));
		params.add("greetings" + EmailToSend.PARAMETER_SEPARATOR +
				   $("DocumentMenuItemActionBehaviors.signatureRequestGreetings", receiverName));
		String senderName = sender.getTitle() != null ? sender.getTitle() : getExternalUserName(sender.getUsername());
		params.add("message" + EmailToSend.PARAMETER_SEPARATOR + $(messageId, senderName, docText, expiration));
		params.add("link" + EmailToSend.PARAMETER_SEPARATOR + link);
		params.add("linkMessage" + EmailToSend.PARAMETER_SEPARATOR + $(linkId));
		params.add("closure" + EmailToSend.PARAMETER_SEPARATOR +
				   $("DocumentMenuItemActionBehaviors.signatureRequestClosure"));

		return params;
	}

	private EmailToSend newEmailToSend() {
		MetadataSchemaTypes schemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		MetadataSchema schema = schemaTypes.getSchemaType(EmailToSend.SCHEMA_TYPE).getDefaultSchema();
		Record emailToSendRecord = recordServices.newRecordWithSchema(schema);
		return new EmailToSend(emailToSendRecord, schemaTypes);
	}

	private String getUrlFromExternalAccess(ExternalAccessUrl externalAccess, String language) {
		String url = modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.CONSTELLIO_URL);

		StringBuilder sb = new StringBuilder();
		sb.append(url);
		sb.append("signatureExternalAccess?id=");
		sb.append(externalAccess.getId());
		sb.append("&token=");
		sb.append(externalAccess.getToken());
		sb.append("&language=");
		sb.append(language);
		return sb.toString();
	}

	private String getExternalUserName(String username) {
		int mailStartIndex = username.lastIndexOf('(');
		return username.substring(0, mailStartIndex - 1);
	}

	private String getExternalUserEmail(String username) {
		int mailStartIndex = username.lastIndexOf('(');
		return username.substring(mailStartIndex + 1, username.length() - 1);
	}

	public boolean isDisableExternalSignatures() {
		return new ESignatureConfigs(configsManager).isDisableExternalSignatures();
	}
}
