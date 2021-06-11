package com.constellio.app.ui.framework.data.event;

import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class EventTypeUtils implements Serializable {

	public static String getEventTypeCaption(String eventType) {
		if (eventType.equals(EventType.OPEN_SESSION)) {
			return $("ListEventsView.openedSessions");
		} else if (eventType.equals(EventType.VIEW_FOLDER)) {
			return $("ListEventsView.foldersView");
		} else if (eventType.equals(EventType.ATTEMPTED_OPEN_SESSION)) {
			return $("ListEventsView.failedLogins");
		} else if (eventType.equals(EventType.CREATE_FOLDER)) {
			return $("ListEventsView.foldersCreation");
		} else if (eventType.equals(EventType.MODIFY_FOLDER)) {
			return $("ListEventsView.foldersModification");
		} else if (eventType.equals(EventType.DELETE_FOLDER)) {
			return $("ListEventsView.foldersDeletion");
		} else if (eventType.equals(EventType.BORROW_FOLDER)) {
			return $("ListEventsView.borrowedFolders");
		} else if (eventType.equals(EventType.RETURN_FOLDER)) {
			return $("ListEventsView.returnedFolders");
		} else if (eventType.equals(EventType.VIEW_DOCUMENT)) {
			return $("ListEventsView.documentsView");
		} else if (eventType.equals(EventType.CREATE_DOCUMENT)) {
			return $("ListEventsView.documentsCreation");
		} else if (eventType.equals(EventType.MODIFY_DOCUMENT)) {
			return $("ListEventsView.documentsModification");
		} else if (eventType.equals(EventType.DELETE_DOCUMENT)) {
			return $("ListEventsView.documentsDeletion");
		} else if (eventType.equals(EventType.CURRENT_BORROW_DOCUMENT)) {
			return $("ListEventsView.currentlyBorrowedDocuments");
		} else if (eventType.equals(EventType.BORROW_DOCUMENT)) {
			return $("ListEventsView.borrowedDocuments");
		} else if (eventType.equals(EventType.RETURN_DOCUMENT)) {
			return $("ListEventsView.returnedDocuments");
		} else if (eventType.equals(EventType.BORROW_CONTAINER)) {
			return $("ListEventsView.borrowedContainers");
		} else if (eventType.equals(EventType.RETURN_CONTAINER)) {
			return $("ListEventsView.returnedContainers");
		} else if (eventType.equals(EventType.CREATE_USER)) {
			return $("ListEventsView.createdUsersEvent");
		} else if (eventType.equals(EventType.MODIFY_USER)) {
			return $("ListEventsView.modifiedUsersEvent");
		} else if (eventType.equals(EventType.DELETE_USER)) {
			return $("ListEventsView.deletedUsersEvent");
		} else if (eventType.equals(EventType.CREATE_GROUP)) {
			return $("ListEventsView.createdGroupsEvent");
		} else if (eventType.equals(EventType.DELETE_GROUP)) {
			return $("ListEventsView.deletedGroupsEvent");
		} else if (eventType.equals(EventType.GRANT_PERMISSION_FOLDER)) {
			return $("ListEventsView.grantedPermissions.folder");
		} else if (eventType.equals(EventType.CREATE_SHARE_FOLDER)) {
			return $("ListEventsView.createShare.folder");
		} else if (eventType.equals(EventType.MODIFY_SHARE_FOLDER)) {
			return $("ListEventsView.modifyShare.folder");
		} else if (eventType.equals(EventType.DELETE_SHARE_FOLDER)) {
			return $("ListEventsView.deleteShare.folder");
		} else if (eventType.equals(EventType.MODIFY_PERMISSION_FOLDER)) {
			return $("ListEventsView.modifiedPermissions.folder");
		} else if (eventType.equals(EventType.DELETE_PERMISSION_FOLDER)) {
			return $("ListEventsView.deletedPermissions.folder");
		} else if (eventType.equals(EventType.GRANT_PERMISSION_DOCUMENT)) {
			return $("ListEventsView.grantedPermissions.document");
		} else if (eventType.equals(EventType.MODIFY_PERMISSION_DOCUMENT)) {
			return $("ListEventsView.modifiedPermissions.document");
		} else if (eventType.equals(EventType.DELETE_PERMISSION_DOCUMENT)) {
			return $("ListEventsView.deletedPermissions.document");
		} else if (eventType.equals(EventType.CREATE_SHARE_DOCUMENT)) {
			return $("ListEventsView.createShare.document");
		} else if (eventType.equals(EventType.MODIFY_SHARE_DOCUMENT)) {
			return $("ListEventsView.modifyShare.document");
		} else if (eventType.equals(EventType.DELETE_SHARE_DOCUMENT)) {
			return $("ListEventsView.deleteShare.document");
		} else if (eventType.equals(EventType.FOLDER_RELOCATION)) {
			return $("ListEventsView.folderRelocation");
		} else if (eventType.equals(EventType.FOLDER_DEPOSIT)) {
			return $("ListEventsView.folderDeposit");
		} else if (eventType.equals(EventType.FOLDER_DESTRUCTION)) {
			return $("ListEventsView.folderDestruction");
		} else if (eventType.equals(EventType.PDF_A_GENERATION)) {
			return $("ListEventsView.pdfAGeneration");
		} else if (eventType.equals(EventType.RECEIVE_FOLDER)) {
			return $("ListEventsView.receiveFolder");
		} else if (eventType.equals(EventType.RECEIVE_CONTAINER)) {
			return $("ListEventsView.receiveContainer");
		} else if (eventType.equals(EventType.CURRENTLY_BORROWED_FOLDERS)) {
			return $("ListEventsView.currentlyBorrowedFolders");
		} else if (eventType.equals(EventType.LATE_BORROWED_FOLDERS)) {
			return $("ListEventsView.lateBorrowedFolders");
		} else if (eventType.equals(EventType.CONSULTATION_FOLDER)) {
			return $("ListEventsView.consultationFolders");
		} else if (eventType.equals(EventType.CREATE_TASK)) {
			return $("ListEventsView.createTask");
		} else if (eventType.equals(EventType.MODIFY_TASK)) {
			return $("ListEventsView.modifyTask");
		} else if (eventType.equals(EventType.DELETE_TASK)) {
			return $("ListEventsView.deleteTask");
		} else if (eventType.equals(EventType.REINDEXING)) {
			return $("ListEventsView.reindexing");
		} else if (eventType.equals(EventType.RESTARTING)) {
			return $("ListEventsView.restarting");
		} else if (eventType.equals(EventType.BORROW_REQUEST_FOLDER)) {
			return $("ListEventsView.borrowRequestFolder");
		} else if (eventType.equals(EventType.RETURN_REQUEST_FOLDER)) {
			return $("ListEventsView.returnRequestFolder");
		} else if (eventType.equals(EventType.REACTIVATION_REQUEST_FOLDER)) {
			return $("ListEventsView.reactivationRequestFolder");
		} else if (eventType.equals(EventType.BORROW_EXTENSION_REQUEST_FOLDER)) {
			return $("ListEventsView.borrowExtensionRequestFolder");
		} else if (eventType.equals(EventType.BORROW_REQUEST_CONTAINER)) {
			return $("ListEventsView.borrowRequestContainer");
		} else if (eventType.equals(EventType.RETURN_REQUEST_CONTAINER)) {
			return $("ListEventsView.returnRequestContainer");
		} else if (eventType.equals(EventType.REACTIVATION_REQUEST_CONTAINER)) {
			return $("ListEventsView.reactivationRequestContainer");
		} else if (eventType.equals(EventType.BORROW_EXTENSION_REQUEST_CONTAINER)) {
			return $("ListEventsView.borrowExtensionRequestContainer");
		} else if (eventType.equals(EventType.OPEN_DOCUMENT)) {
			return $("ListEventsView.openDocument");
		} else if (eventType.equals(EventType.DOWNLOAD_DOCUMENT)) {
			return $("ListEventsView.downloadDocument");
		} else if (eventType.equals(EventType.UPLOAD_DOCUMENT)) {
			return $("ListEventsView.uploadDocument");
		} else if (eventType.equals(EventType.SHARE_DOCUMENT)) {
			return $("ListEventsView.shareDocument");
		} else if (eventType.equals(EventType.FINALIZE_DOCUMENT)) {
			return $("ListEventsView.finalizeDocument");
		} else if (eventType.equals(EventType.SHARE_FOLDER)) {
			return $("ListEventsView.shareFolder");
		} else if (eventType.equals(EventType.BATCH_PROCESS_CREATED)) {
			return $("ListEventsView.batchProcessEvents.created");
		} else if (eventType.equals(EventType.SIGN_DOCUMENT)) {
			return $("ListEventsView.signature");
		} else if (eventType.equals(EventType.SIGNATURE_REQUEST)) {
			return $("ListEventsView.signatureRequest");
		} else if (eventType.equals(EventType.EMPTY_DOCUMENT)) {
			return $("ListEventsView.emptyDocuments");
		} else if (eventType.equals(EventType.SCANNED_DOCUMENT)) {
			return $("ListEventsView.scannedDocument");
		} else {
			throw new UnsupportedEventTypeRuntimeException(eventType);
		}
	}

	public static List<String> getDisplayedMetadataCodes(MetadataSchema metadataSchema, String eventType) {
		List<String> metadataCodes = new ArrayList<>();
		metadataCodes.addAll(getCommunMetadata(metadataSchema));
		metadataCodes.addAll(getSpecificMetadata(metadataSchema, eventType));
		return metadataCodes;
	}

	private static List<String> getCommunMetadata(MetadataSchema metadataSchema) {
		List<String> metadataCodes = new ArrayList<>();
		Metadata eventUserNameMetadata = metadataSchema.getMetadata(Event.USERNAME);
		Metadata eventUserIPMetadata = metadataSchema.getMetadata(Event.IP);
		Metadata eventDateMetadata = metadataSchema.getMetadata(Schemas.CREATED_ON.getLocalCode());
		Metadata eventUserRolesMetadata = metadataSchema.getMetadata(Event.USER_ROLES);
		Metadata eventType = metadataSchema.getMetadata(Event.TYPE);
		metadataCodes.add(eventUserNameMetadata.getCode());
		metadataCodes.add(eventDateMetadata.getCode());
		metadataCodes.add(eventUserRolesMetadata.getCode());
		metadataCodes.add(eventUserIPMetadata.getCode());
		metadataCodes.add(eventType.getCode());
		return metadataCodes;
	}

	private static List<String> getSpecificMetadata(MetadataSchema metadataSchema,
													String eventType) {
		List<String> metadataCodes = new ArrayList<>();
		if (isPermissionEvent(eventType)) {
			metadataCodes.addAll(getEventRecordMetadata(metadataSchema));
			metadataCodes.addAll(getEventPermissionMetadata(metadataSchema));
		} else if (isRecordEvent(eventType)) {
			metadataCodes.addAll(getEventRecordMetadata(metadataSchema));
			if (eventType.equals(EventType.DELETE_FOLDER) || eventType.equals(EventType.DELETE_DOCUMENT)) {
				Metadata reasonMetadata = metadataSchema.getMetadata(Event.REASON);
				metadataCodes.add(reasonMetadata.getCode());
			} else if (isPotentiallyFromRequestTask(eventType)) {
				Metadata taskMetadata = metadataSchema.getMetadata(Event.TASK);
				metadataCodes.add(taskMetadata.getCode());
				Metadata receiverMetadata = metadataSchema.getMetadata(Event.RECEIVER_NAME);
				metadataCodes.add(receiverMetadata.getCode());
				Metadata descriptionMetadata = metadataSchema.getMetadata(Event.REASON);
				metadataCodes.add(descriptionMetadata.getCode());
			} else if (eventType.equals(EventType.SIGN_DOCUMENT)) {
				metadataCodes.add(Event.USERNAME);
				metadataCodes.add(Event.IP);
			} else if (eventType.equals(EventType.SCANNED_DOCUMENT)) {
				metadataCodes.add(Event.PAGE_COUNT);
			}
		} else if (isUserEvent(eventType) ||
				   isGroupEvent(eventType)) {
			metadataCodes.addAll(getEventUserMetadata(metadataSchema));
		}
		if (isModificationEvent(eventType)) {
			metadataCodes.add(Event.DELTA);
		}
		if (isRequestTaskEvent(eventType)) {
			metadataCodes.add(Event.ACCEPTED);
		}
		if (isBatchProcessEvent(eventType)) {
			metadataCodes.addAll(getEventBatchProcessMetadata(metadataSchema));
		}
		if (isAuthenticationEvent(eventType)) {
			metadataCodes.add(Event.USERNAME);
			metadataCodes.add(Event.IP);
		}
		return metadataCodes;
	}

	private static boolean isAuthenticationEvent(String eventType) {
		return asList(EventType.ATTEMPTED_OPEN_SESSION).contains(eventType);
	}

	private static boolean isBatchProcessEvent(String eventType) {
		return asList(EventType.BATCH_PROCESS_CREATED)
				.contains(eventType);
	}

	private static boolean isRequestTaskEvent(String eventType) {
		return asList(EventType.BORROW_REQUEST_FOLDER, EventType.RETURN_REQUEST_FOLDER, EventType.REACTIVATION_REQUEST_FOLDER, EventType.BORROW_EXTENSION_REQUEST_FOLDER,
				EventType.BORROW_REQUEST_CONTAINER, EventType.RETURN_REQUEST_CONTAINER, EventType.REACTIVATION_REQUEST_CONTAINER, EventType.BORROW_EXTENSION_REQUEST_CONTAINER)
				.contains(eventType);
	}

	private static boolean isPotentiallyFromRequestTask(String eventType) {
		return asList(EventType.BORROW_FOLDER, EventType.BORROW_CONTAINER, EventType.RETURN_FOLDER, EventType.RETURN_CONTAINER,
				EventType.BORROW_REQUEST_FOLDER, EventType.RETURN_REQUEST_FOLDER, EventType.REACTIVATION_REQUEST_FOLDER, EventType.BORROW_EXTENSION_REQUEST_FOLDER,
				EventType.BORROW_REQUEST_CONTAINER, EventType.RETURN_REQUEST_CONTAINER, EventType.REACTIVATION_REQUEST_CONTAINER, EventType.BORROW_EXTENSION_REQUEST_CONTAINER)
				.contains(eventType);
	}

	private static List<String> getEventBatchProcessMetadata(MetadataSchema metadataSchema) {
		List<String> metadataCodes = new ArrayList<>();

		Metadata eventProcessIdMetadata = metadataSchema.getMetadata(Event.BATCH_PROCESS_ID);
		metadataCodes.add(eventProcessIdMetadata.getCode());

		Metadata eventTotalRecordMetadata = metadataSchema.getMetadata(Event.TOTAL_MODIFIED_RECORD);
		metadataCodes.add(eventTotalRecordMetadata.getCode());

		Metadata eventContentMetadata = metadataSchema.getMetadata(Event.CONTENT);
		metadataCodes.add(eventContentMetadata.getCode());

		return metadataCodes;
	}

	private static List<String> getEventUserMetadata(MetadataSchema metadataSchema) {
		List<String> metadataCodes = new ArrayList<>();
		Metadata eventTitleMetadata = metadataSchema.getMetadata(Schemas.TITLE.getLocalCode());
		metadataCodes.add(eventTitleMetadata.getCode());
		return metadataCodes;
	}

	public static boolean isUserEvent(String eventType) {
		if (eventType.contains("_user")) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isGroupEvent(String eventType) {
		if (eventType.contains("_group")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isRecordEvent(String eventType) {
		return isFolderEvent(eventType) ||
			   isDocumentEvent(eventType) ||
			   isTaskEvent(eventType) ||
			   isContainerEvent(eventType) ||
			   eventType.equals(EventType.PDF_A_GENERATION);
	}

	private static boolean isContainerEvent(String eventType) {
		//FIXME list all events
		if (eventType.contains("_container")) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isDocumentEvent(String eventType) {
		//FIXME list all events
		if (eventType.contains("_document")) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isTaskEvent(String eventType) {
		//FIXME list all events
		if (eventType.contains("_userTask")) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isFolderEvent(String eventType) {
		//FIXME list all events
		if (eventType.contains("_folder")) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isPermissionEvent(String eventType) {
		if (eventType.contains(EventType.DELETE_PERMISSION) ||
			eventType.contains(EventType.GRANT_PERMISSION) ||
			eventType.contains(EventType.MODIFY_PERMISSION) ||
			eventType.contains(EventType.CREATE_SHARE) ||
			eventType.contains(EventType.MODIFY_SHARE) ||
			eventType.contains(EventType.DELETE_SHARE)) {
			return true;
		} else {
			return false;
		}
	}

	private static List<String> getEventPermissionMetadata(MetadataSchema metadataSchema) {
		List<String> metadataCodes = new ArrayList<>();
		Metadata permissionDatesMetadata = metadataSchema.getMetadata(Event.PERMISSION_DATE_RANGE);
		Metadata permissionRolesMetadata = metadataSchema.getMetadata(Event.PERMISSION_ROLES);
		Metadata permissionUsersMetadata = metadataSchema.getMetadata(Event.PERMISSION_USERS);
		Metadata negativeAuthMetadata = metadataSchema.getMetadata(Event.NEGATIVE_AUTHORIZATION);
		metadataCodes.add(permissionDatesMetadata.getCode());
		metadataCodes.add(permissionRolesMetadata.getCode());
		metadataCodes.add(permissionUsersMetadata.getCode());
		metadataCodes.add(negativeAuthMetadata.getCode());
		return metadataCodes;
	}

	private static List<String> getEventRecordMetadata(MetadataSchema metadataSchema) {
		List<String> metadataCodes = new ArrayList<>();
		Metadata recordIdMetadata = metadataSchema.getMetadata(Event.RECORD_ID);
		Metadata eventTitleMetadata = metadataSchema.getMetadata(Schemas.TITLE.getLocalCode());
		metadataCodes.add(recordIdMetadata.getCode());
		metadataCodes.add(eventTitleMetadata.getCode());
		return metadataCodes;
	}

	public static boolean isModificationEvent(String eventType) {
		//FIXME list all events
		if (eventType.contains("modify_")) {
			return true;
		} else if (asList(EventType.BORROW_FOLDER, EventType.BORROW_CONTAINER,
				EventType.BORROW_REQUEST_FOLDER, EventType.BORROW_EXTENSION_REQUEST_FOLDER,
				EventType.BORROW_REQUEST_CONTAINER, EventType.BORROW_EXTENSION_REQUEST_CONTAINER)
				.contains(eventType)) {
			return true;
		} else {
			return false;
		}
	}
}
