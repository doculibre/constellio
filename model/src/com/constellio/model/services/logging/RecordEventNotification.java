package com.constellio.model.services.logging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

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
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordEventNotification {
	
	private static Map<String, Map<String, RecordEventNotification>> allRecordEventNotifications = new HashMap<>();
	
	private static ModelLayerFactory modelLayerFactory;
	
	private static RecordServices recordServices;
	
	private static ConstellioEIMConfigs eimConfigs;
	
	private String recordId;
	
	private String userId;
	
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String computeMessage() {
		return getMessage();
	}
	
	public void sendNotifications(Event event) {
		if (appliesTo(event)) {
			EmailToSend emailToSend = prepareEmailToSend();
			saveEmailToSend(emailToSend);
		}
	}
	
	public boolean appliesTo(Event event) {
		return false;
	}

	private EmailToSend prepareEmailToSend() {
		Record record = recordServices.getDocumentById(recordId);
		Record userRecord = recordServices.getDocumentById(userId);
		String collection = record.getCollection();
		
		Metadata emailMetadata = getTypes(collection).getSchemaOf(userRecord).getMetadata(User.EMAIL);
		String email = userRecord.get(emailMetadata);
		EmailAddress emailAddress = new EmailAddress(userRecord.getTitle(), email);
		
		EmailToSend emailToSend = newEmailToSend(collection).setTryingCount(0d);
		List<EmailAddress> followersEmails = Arrays.asList(emailAddress);
		emailToSend.setTo(followersEmails);
		emailToSend.setSendOn(TimeProvider.getLocalDateTime());
//		emailToSend.setTemplate(templateId);
		emailToSend.setBody(computeMessage());
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
			if (recordEventNotifications != null) {
				for (RecordEventNotification recordEventNotification : recordEventNotifications.values()) {
					recordEventNotification.sendNotifications(event);
				}
			}
		}
	}

	public static class DocumentEventNotification extends RecordEventNotification {
		
		public static final String DEFAULT_MESSAGE = "Le document {title} a été modifié. Cliquer sur ce lien pour consulter le document: {link}";
		
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
				if (isEditDocument() && EventType.MODIFY.equals(eventType)) {
					applies = true;
				} else if (isDeleteDocument() && EventType.DELETE.equals(eventType)) {
					applies = true;
				} else {
					applies = false;
				}
			} else {
				applies = false;
			}
			return applies;
		}
		
		@Override
		public String computeMessage() {
			String message = super.computeMessage();
			if (StringUtils.isBlank(message)) {
				message = DEFAULT_MESSAGE;
				
				String recordId = getRecordId();
				Record record = recordServices.getDocumentById(recordId);
				String constellioUrl = StringUtils.appendIfMissing(eimConfigs.getConstellioUrl(), "/");
				String displayUrl = constellioUrl + "/#!displayDocument/" + recordId;
				message = StringUtils.replace(message, "{title}", record.getIdTitle());
				message = StringUtils.replace(message, "{link}", displayUrl);
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
		
		public static final String DEFAULT_MESSAGE = "Le dossier {title} a été modifié. Cliquer sur ce lien pour consulter le dossier: {link}";
		
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
				if (isEditFolder() && EventType.MODIFY.equals(eventType)) {
					applies = true;
				} else if (isDeleteFolder() && EventType.DELETE.equals(eventType)) {
					applies = true;
				} else {
					applies = false;
				}
			} else if ("folder".equals(schemaTypeCode)) {
				Metadata parentFolderMetadata = getTypes(collection).getSchemaOf(record).getMetadata("parentFolder");
				String parentFolder = record.get(parentFolderMetadata);
				if (getRecordId().equals(parentFolder)) {
					if (isAddSubFolder() && EventType.CREATE.equals(eventType)) {
						applies = true;
					} else if (isEditSubFolder() && EventType.MODIFY.equals(eventType)) {
						applies = true;
					} else if (isDeleteSubFolder() && EventType.DELETE.equals(eventType)) {
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
					if (isAddDocument() && EventType.CREATE.equals(eventType)) {
						applies = true;
					} else if (isEditDocument() && EventType.MODIFY.equals(eventType)) {
						applies = true;
					} else if (isDeleteDocument() && EventType.DELETE.equals(eventType)) {
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

		@Override
		public String computeMessage() {
			String message = super.computeMessage();
			if (StringUtils.isBlank(message)) {
				message = DEFAULT_MESSAGE;
				
				String recordId = getRecordId();
				Record record = recordServices.getDocumentById(recordId);
				String constellioUrl = StringUtils.appendIfMissing(eimConfigs.getConstellioUrl(), "/");
				String displayUrl = constellioUrl + "/#!displayFolder/" + recordId;
				message = StringUtils.replace(message, "{title}", record.getIdTitle());
				message = StringUtils.replace(message, "{link}", displayUrl);
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

