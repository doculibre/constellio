package com.constellio.model.services.logging;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RecordEventNotification {
	
	private static Map<String, Map<String, RecordEventNotification>> allRecordEventNotifications = new HashMap<>();
	
	private static ModelLayerFactory modelLayerFactory;
	
	private static RecordServices recordServices;
	
	private static ConstellioEIMConfigs eimConfigs;
	
	private String recordId;
	
	private String userId;

	private String subject;
	
	private String message;
	
	public RecordEventNotification(String recordId, String userId) {
		this.recordId = recordId;
		this.userId = userId;
	}
	
	public String getRecordId() {
		return recordId;
	}

	public String getUserId() {
		return userId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String title) {
		this.subject = title;
	}

	public String computeSubject(Event event) {
		return getSubject();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String computeMessage(Event event) {
		return getMessage();
	}
	
	public void sendNotifications(Event event) {
		if (appliesTo(event)) {
			EmailToSend emailToSend = prepareEmailToSend(event);
			saveEmailToSend(emailToSend);
		}
	}
	
	public boolean appliesTo(Event event) {
		return false;
	}

	private EmailToSend prepareEmailToSend(Event event) {
		Record record = recordServices.getDocumentById(recordId);
		Record userRecord = recordServices.getDocumentById(userId);
		String collection = record.getCollection();
		
		Metadata emailMetadata = getTypes(collection).getSchemaOf(userRecord).getMetadata(User.EMAIL);
		String email = userRecord.get(emailMetadata);
		EmailAddress emailAddress = new EmailAddress(userRecord.getTitle(), email);

		String subject = computeSubject(event);
		String message = computeMessage(event);
		
		EmailToSend emailToSend = newEmailToSend(collection).setTryingCount(0d);
		List<EmailAddress> followersEmails = Arrays.asList(emailAddress);
		emailToSend.setTo(followersEmails);
		emailToSend.setSendOn(TimeProvider.getLocalDateTime());
		emailToSend.setSubject(subject);
//		emailToSend.setTemplate(templateId);

		String messageTemplate = "<html xmlns=\"http://www.w3.org/1999/xhtml\">\r\n" +
								 "<head>\r\n" +
								 "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\r\n" +
								 "    <title>T&acirc;che compl&eacute;t&eacute;e : ${taskTitle}</title>\r\n" +
								 "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\r\n" +
								 "</head>\r\n" +
								 "<body style=\"font-family: 'JF Flat', 'Open Sans', sans-serif, FontAwesome; font-size: 16px; font-style: normal; font-variant: normal; font-weight: bold;\">\r\n" +
								 "<table width=\"100%\" height=\"100%\" cellpadding=\"30\" cellspacing=\"0\">\r\n" +
								 "    <tr>\r\n" +
								 "        <td background=\"cid:background\" align=\"center\" valign=\"top\">\r\n" +
								 "            <table bgcolor=\"white\" width=\"100%\" cellpadding=\"20\">\r\n" +
								 "                <tr>\r\n" +
								 "                    <td align=\"left\">\r\n" +
								 "                        <table width=\"100%\" height=\"100%\" cellpadding=\"10\">\r\n" +
								 "                            <tr>\r\n" +
								 "                                <td valign=\"center\">\r\n" +
								 "                                    <div style=\"-webkit-user-select: text; box-sizing: border-box; color: rgb(25, 125, 225); cursor: default; display: inline-block; font-family: 'JF Flat', 'Open Sans', sans-serif, FontAwesome; font-size: 25.6000003814697px; font-style: normal; font-variant: normal; font-weight: 200; height: 27.27272605896px; letter-spacing: -0.512000024318695px; line-height: 25.5999984741211px; margin-bottom: 19.7119998931885px;\">\r\n" +
								 "                                        ${subject}\r\n" +
								 "                                    </div>\r\n" +
								 "                                </td>\r\n" +
								 "                                <td align=\"right\" valign=\"top\">\r\n" +
								 "                                    <a href=\"${constellioUrl}\" style=\"text-decoration:none;\"><img border=\"0\" src=\"cid:logo\"/></a>\r\n" +
								 "                                </td>\r\n" +
								 "                            </tr>\r\n" +
								 "                            <tr>\r\n" +
								 "                                <td colspan=\"2\">\r\n" +
								 "                                    <p>${messageBody}</p>\r\n" +
								 "                                </td>\r\n" +
								 "                            </tr>\r\n" +
								 "                            <tr>\r\n" +
								 "                                <td colspan=\"2\" align=\"center\" valign=\"center\">\r\n" +
								 "                                    <div>\r\n" +
								 "                                        <a href=\"http://constellio.com\"\r\n" +
								 "                                           style=\"text-decoration:none;\">Propuls&eacute; par Constellio</a>\r\n" +
								 "                                    </div>\r\n" +
								 "                                </td>\r\n" +
								 "                            </tr>\r\n" +
								 "                        </table>\r\n" +
								 "                    </td>\r\n" +
								 "                </tr>\r\n" +
								 "            </table>\r\n" +
								 "        </td>\r\n" +
								 "    </tr>\r\n" +
								 "</table>\r\n" +
								 "</body>\r\n" +
								 "</html>";
		String constellioUrl = StringUtils.appendIfMissing(eimConfigs.getConstellioUrl(), "/");
		String messageFromTemplate = messageTemplate;
		messageFromTemplate = StringUtils.replace(messageFromTemplate, "${subject}", subject);
		messageFromTemplate = StringUtils.replace(messageFromTemplate, "${messageBody}", message);
		messageFromTemplate = StringUtils.replace(messageFromTemplate, "${constellioUrl}", constellioUrl);
		emailToSend.setBody(messageFromTemplate);
		return emailToSend;
	}

	private void saveEmailToSend(EmailToSend emailToSend) {
		Transaction transaction = new Transaction();
		transaction.setRecordFlushing(RecordsFlushing.LATER());
		transaction.add(emailToSend);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void init(ModelLayerFactory modelLayerFactory) {
		if (RecordEventNotification.modelLayerFactory == null) {
			RecordEventNotification.modelLayerFactory = modelLayerFactory;
			recordServices = modelLayerFactory.newRecordServices();
			eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		}
	}

	private static MetadataSchemaTypes getTypes(String collection) {
		return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
	}

	private static MetadataSchema defaultSchema(String code, String collection) {
		return getTypes(collection).getSchema(code + "_default");
	}

	private static Record create(MetadataSchema schema) {
		return modelLayerFactory.newRecordServices().newRecordWithSchema(schema);
	}
	
	private static EmailToSend newEmailToSend(String collection) {
		return new EmailToSend(create(defaultSchema(EmailToSend.SCHEMA_TYPE, collection)), getTypes(collection));
	}
	
	public static RecordEventNotification get(String recordId, String userId) {
		RecordEventNotification result;
		Map<String, RecordEventNotification> recordEventNotifications = allRecordEventNotifications.get(recordId);
		if (recordEventNotifications != null) {
			result = recordEventNotifications.get(userId);
		} else {
			result = null;
		}
		return result;
	}

	public static void addUpdateNotification(RecordEventNotification notification) {
		String recordId = notification.getRecordId();
		String userId = notification.getUserId();
		Map<String, RecordEventNotification> recordEventNotifications = allRecordEventNotifications.get(recordId);
		if (recordEventNotifications == null) {
			recordEventNotifications = new HashMap<>();
			allRecordEventNotifications.put(recordId, recordEventNotifications);
		}
		recordEventNotifications.put(userId, notification);
	}
	
	public static void sendNotifications(Event event, ModelLayerFactory modelLayerFactory) {
		init(modelLayerFactory);
		String recordId = event.getRecordId();
		if (recordId != null) {
			Map<String, RecordEventNotification> recordEventNotifications = allRecordEventNotifications.get(recordId);
			if (recordEventNotifications == null) {
				try {
					Record record = recordServices.getDocumentById(recordId);
					List<String> pathParts = record.getList(Schemas.PATH_PARTS);
					for (Iterator<String> it = pathParts.iterator(); it.hasNext(); ) {
						String pathPart = it.next();
						if (!pathPart.equals(recordId)) {
							recordEventNotifications = allRecordEventNotifications.get(pathPart);
							if (recordEventNotifications != null) {
								break;
							}
						}
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			if (recordEventNotifications != null) {
				for (RecordEventNotification recordEventNotification : recordEventNotifications.values()) {
					recordEventNotification.sendNotifications(event);
				}
			}
		}
	}

	public static class DocumentEventNotification extends RecordEventNotification {

		public static final String DEFAULT_SUBJECT = "Des changements visant le document {title} sont survenus.";

		public static final String DEFAULT_MESSAGE = "{eventType}. Cliquer sur <a href=\"{link}\">ce lien</a> pour consulter le document";
		
		private boolean editDocument;
		
		private boolean deleteDocument;
		
		public DocumentEventNotification(String documentId, String userId) {
			super(documentId, userId);
		}
		
		@Override
		public boolean appliesTo(Event event) {
			boolean applies;
			String eventType = event.getType();
			String recordId = event.getRecordId();
			if (recordId.equals(getRecordId())) {
				if (isEditDocument() && (EventType.MODIFY.equals(eventType) || EventType.MODIFY_DOCUMENT.equals(eventType))) {
					applies = true;
				} else if (isDeleteDocument() && (EventType.DELETE.equals(eventType) || EventType.DELETE_DOCUMENT.equals(eventType))) {
					applies = true;
				} else {
					applies = false;
				}
			} else {
				applies = false;
			}
			return applies;
		}

		private String getEventTypeText(Event event) {
			String eventTypeText;
			String eventType = event.getType();
			if (EventType.MODIFY.equals(eventType) || EventType.MODIFY_DOCUMENT.equals(eventType)) {
				eventTypeText = "Modification";
			} else if (EventType.DELETE.equals(eventType) || EventType.DELETE_DOCUMENT.equals(eventType)) {
				eventTypeText = "Suppression";
			} else {
				eventTypeText = null;
			}
			return eventTypeText;
		} 

		@Override
		public String computeSubject(Event event) {
			String subject = super.computeSubject(event);
			if (StringUtils.isBlank(subject)) {
				subject = DEFAULT_SUBJECT;

				String recordId = getRecordId();
				Record record = recordServices.getDocumentById(recordId);
				subject = StringUtils.replace(subject, "{title}", record.getTitle());
			}
			return subject;
		}

		@Override
		public String computeMessage(Event event) {
			String textMessage = getMessage();
			String message = DEFAULT_MESSAGE;

			String recordId = getRecordId();
			Record record = recordServices.getDocumentById(recordId);
			String constellioUrl = StringUtils.appendIfMissing(eimConfigs.getConstellioUrl(), "/");
			String displayUrl = constellioUrl + "/#!displayDocument/" + recordId;
			message = StringUtils.replace(message, "{title}", record.getTitle());
			message = StringUtils.replace(message, "{eventType}", getEventTypeText(event));
			message = StringUtils.replace(message, "{link}", displayUrl);
			if (StringUtils.isNotBlank(textMessage)) {
				message += "<p>" + textMessage + "</p>";
			}
			return message;
		}

		public boolean isEditDocument() {
			return editDocument;
		}

		public void setEditDocument(boolean editDocument) {
			this.editDocument = editDocument;
		}

		public boolean isDeleteDocument() {
			return deleteDocument;
		}

		public void setDeleteDocument(boolean deleteDocument) {
			this.deleteDocument = deleteDocument;
		}

	}
	
	public static class FolderEventNotification extends RecordEventNotification {

		public static final String DEFAULT_SUBJECT = "Des changements visant le dossier {title} sont survenus";

		public static final String DEFAULT_MESSAGE = "{eventType}. Cliquer sur <a href=\"{link}\">ce lien</a> pour consulter le dossier.";
		
		private boolean editFolder;
		
		private boolean deleteFolder;
		
		private boolean addDocument;
		
		private boolean editDocument;
		
		private boolean deleteDocument;
		
		private boolean addSubFolder;
		
		private boolean editSubFolder;
		
		private boolean deleteSubFolder;
		
		public FolderEventNotification(String folderId, String userId) {
			super(folderId, userId);
		}
		
		@Override
		public boolean appliesTo(Event event) {
			boolean applies;
			String eventType = event.getType();
			String recordId = event.getRecordId();
			Record record = recordServices.getDocumentById(recordId);
			String collection = record.getCollection();
			String schemaTypeCode = SchemaUtils.getSchemaTypeCode(record.getSchemaCode());
			if (recordId.equals(getRecordId())) {
				if (isEditFolder() && (EventType.MODIFY.equals(eventType) || EventType.MODIFY_FOLDER.equals(eventType))) {
					applies = true;
				} else if (isDeleteFolder() && (EventType.DELETE.equals(eventType) || EventType.DELETE_FOLDER.equals(eventType))) {
					applies = true;
				} else {
					applies = false;
				}
			} else if ("folder".equals(schemaTypeCode)) {
				Metadata parentFolderMetadata = getTypes(collection).getSchemaOf(record).getMetadata("parentFolder");
				String parentFolder = record.get(parentFolderMetadata);
				if (getRecordId().equals(parentFolder)) {
					if (isAddSubFolder() && (EventType.CREATE.equals(eventType) || EventType.CREATE_FOLDER.equals(eventType))) {
						applies = true;
					} else if (isEditSubFolder() && (EventType.MODIFY.equals(eventType) || EventType.MODIFY_FOLDER.equals(eventType))) {
						applies = true;
					} else if (isDeleteSubFolder() && (EventType.DELETE.equals(eventType) || EventType.DELETE_FOLDER.equals(eventType))) {
						applies = true;
					} else {
						applies = false;
					}
				} else {
					applies = false;
				}
			} else if ("document".equals(schemaTypeCode)) {
				Metadata folderMetadata = getTypes(collection).getSchemaOf(record).getMetadata("folder");
				String folder = record.get(folderMetadata);
				if (getRecordId().equals(folder)) {
					if (isAddDocument() && (EventType.CREATE.equals(eventType) || EventType.CREATE_DOCUMENT.equals(eventType))) {
						applies = true;
					} else if (isEditDocument() && (EventType.MODIFY.equals(eventType) || EventType.MODIFY_DOCUMENT.equals(eventType))) {
						applies = true;
					} else if (isDeleteDocument() && (EventType.DELETE.equals(eventType) || EventType.DELETE_DOCUMENT.equals(eventType))) {
						applies = true;
					} else {
						applies = false;
					}
				} else {
					applies = false;
				}	
			} else {
				applies = false;
			}
			return applies;
		}

		private String getEventTypeText(Event event) {
			String eventTypeText;
			String eventType = event.getType();
			String recordId = event.getRecordId();
			Record record = recordServices.getDocumentById(recordId);
			String collection = record.getCollection();
			String schemaTypeCode = SchemaUtils.getSchemaTypeCode(record.getSchemaCode());
			if (recordId.equals(getRecordId())) {
				if (isEditFolder() && (EventType.MODIFY.equals(eventType) || EventType.MODIFY_FOLDER.equals(eventType))) {
					eventTypeText = "Modification du dossier";
				} else if (isDeleteFolder() && (EventType.DELETE.equals(eventType) || EventType.DELETE_FOLDER.equals(eventType))) {
					eventTypeText = "Suppression du dossier";
				} else {
					eventTypeText = null;
				}
			} else if ("folder".equals(schemaTypeCode)) {
				Metadata parentFolderMetadata = getTypes(collection).getSchemaOf(record).getMetadata("parentFolder");
				String parentFolder = record.get(parentFolderMetadata);
				if (getRecordId().equals(parentFolder)) {
					if (isAddSubFolder() && (EventType.CREATE.equals(eventType) || EventType.CREATE_FOLDER.equals(eventType))) {
						eventTypeText = "Ajout d'un sous-dossier: " + record.getTitle();
					} else if (isEditSubFolder() && (EventType.MODIFY.equals(eventType) || EventType.MODIFY_FOLDER.equals(eventType))) {
						eventTypeText = "Modification d'un sous-dossier: " + record.getTitle();
					} else if (isDeleteSubFolder() && (EventType.DELETE.equals(eventType) || EventType.DELETE_FOLDER.equals(eventType))) {
						eventTypeText = "Suppression d'un sous-dossier: " + record.getTitle();
					} else {
						eventTypeText = null;
					}
				} else {
					eventTypeText = null;
				}
			} else if ("document".equals(schemaTypeCode)) {
				Metadata folderMetadata = getTypes(collection).getSchemaOf(record).getMetadata("folder");
				String folder = record.get(folderMetadata);
				if (getRecordId().equals(folder)) {
					if (isAddDocument() && (EventType.CREATE.equals(eventType) || EventType.CREATE_DOCUMENT.equals(eventType))) {
						eventTypeText = "Ajout d'un document: " + record.getTitle();
					} else if (isEditDocument() && (EventType.MODIFY.equals(eventType) || EventType.MODIFY_DOCUMENT.equals(eventType))) {
						eventTypeText = "Modification d'un document: " + record.getTitle();
						;
					} else if (isDeleteDocument() && (EventType.DELETE.equals(eventType) || EventType.DELETE_DOCUMENT.equals(eventType))) {
						eventTypeText = "Suppression d'un document: " + record.getTitle();
					} else {
						eventTypeText = null;
					}
				} else {
					eventTypeText = null;
				}
			} else {
				eventTypeText = null;
			}
			return eventTypeText;
		} 

		@Override
		public String computeSubject(Event event) {
			String subject = super.computeSubject(event);
			if (StringUtils.isBlank(subject)) {
				subject = DEFAULT_SUBJECT;

				String recordId = getRecordId();
				Record record = recordServices.getDocumentById(recordId);
				subject = StringUtils.replace(subject, "{title}", record.getTitle());
			}
			return subject;
		}

		@Override
		public String computeMessage(Event event) {
			String textMessage = getMessage();
			String message = DEFAULT_MESSAGE;

			String recordId = getRecordId();
			Record record = recordServices.getDocumentById(recordId);
			String constellioUrl = StringUtils.appendIfMissing(eimConfigs.getConstellioUrl(), "/");
			String displayUrl = constellioUrl + "/#!displayFolder/" + recordId;
			message = StringUtils.replace(message, "{title}", record.getTitle());
			message = StringUtils.replace(message, "{eventType}", getEventTypeText(event));
			message = StringUtils.replace(message, "{link}", displayUrl);
			if (StringUtils.isNotBlank(textMessage)) {
				message += "<p>" + textMessage + "</p>";
			}
			return message;
		}

		public boolean isEditFolder() {
			return editFolder;
		}

		public void setEditFolder(boolean editFolder) {
			this.editFolder = editFolder;
		}

		public boolean isDeleteFolder() {
			return deleteFolder;
		}

		public void setDeleteFolder(boolean deleteFolder) {
			this.deleteFolder = deleteFolder;
		}

		public boolean isAddDocument() {
			return addDocument;
		}

		public void setAddDocument(boolean addDocument) {
			this.addDocument = addDocument;
		}

		public boolean isEditDocument() {
			return editDocument;
		}

		public void setEditDocument(boolean editDocument) {
			this.editDocument = editDocument;
		}

		public boolean isDeleteDocument() {
			return deleteDocument;
		}

		public void setDeleteDocument(boolean deleteDocument) {
			this.deleteDocument = deleteDocument;
		}

		public boolean isAddSubFolder() {
			return addSubFolder;
		}

		public void setAddSubFolder(boolean addSubFolder) {
			this.addSubFolder = addSubFolder;
		}

		public boolean isEditSubFolder() {
			return editSubFolder;
		}

		public void setEditSubFolder(boolean editSubFolder) {
			this.editSubFolder = editSubFolder;
		}

		public boolean isDeleteSubFolder() {
			return deleteSubFolder;
		}

		public void setDeleteSubFolder(boolean deleteSubFolder) {
			this.deleteSubFolder = deleteSubFolder;
		}
		
	}

}

