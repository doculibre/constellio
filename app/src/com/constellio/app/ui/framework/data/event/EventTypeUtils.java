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
		switch (eventType) {
			case EventType.OPEN_SESSION:
				return $("ListEventsView.openedSessions");
			case EventType.ATTEMPTED_OPEN_SESSION:
				return $("ListEventsView.failedLogins");
			case EventType.VIEW_FOLDER:
				return $("ListEventsView.foldersView");
			case EventType.CREATE_FOLDER:
				return $("ListEventsView.foldersCreation");
			case EventType.MODIFY_FOLDER:
				return $("ListEventsView.foldersModification");
			case EventType.DELETE_FOLDER:
				return $("ListEventsView.foldersDeletion");
			case EventType.BORROW_FOLDER:
				return $("ListEventsView.borrowedFolders");
			case EventType.RETURN_FOLDER:
				return $("ListEventsView.returnedFolders");
			case EventType.VIEW_DOCUMENT:
				return $("ListEventsView.documentsView");
			case EventType.CREATE_DOCUMENT:
				return $("ListEventsView.documentsCreation");
			case EventType.MODIFY_DOCUMENT:
				return $("ListEventsView.documentsModification");
			case EventType.DELETE_DOCUMENT:
				return $("ListEventsView.documentsDeletion");
			case EventType.CURRENT_BORROW_DOCUMENT:
				return $("ListEventsView.currentlyBorrowedDocuments");
			case EventType.BORROW_DOCUMENT:
				return $("ListEventsView.borrowedDocuments");
			case EventType.RETURN_DOCUMENT:
				return $("ListEventsView.returnedDocuments");
			case EventType.BORROW_CONTAINER:
				return $("ListEventsView.borrowedContainers");
			case EventType.RETURN_CONTAINER:
				return $("ListEventsView.returnedContainers");
			case EventType.CREATE_USER:
				return $("ListEventsView.createdUsersEvent");
			case EventType.MODIFY_USER:
				return $("ListEventsView.modifiedUsersEvent");
			case EventType.DELETE_USER:
				return $("ListEventsView.deletedUsersEvent");
			case EventType.CREATE_GROUP:
				return $("ListEventsView.createdGroupsEvent");
			case EventType.DELETE_GROUP:
				return $("ListEventsView.deletedGroupsEvent");
			case EventType.GRANT_PERMISSION_FOLDER:
				return $("ListEventsView.grantedPermissions.folder");
			case EventType.MODIFY_PERMISSION_FOLDER:
				return $("ListEventsView.modifiedPermissions.folder");
			case EventType.DELETE_PERMISSION_FOLDER:
				return $("ListEventsView.deletedPermissions.folder");
			case EventType.GRANT_PERMISSION_DOCUMENT:
				return $("ListEventsView.grantedPermissions.document");
			case EventType.MODIFY_PERMISSION_DOCUMENT:
				return $("ListEventsView.modifiedPermissions.document");
			case EventType.DELETE_PERMISSION_DOCUMENT:
				return $("ListEventsView.deletedPermissions.document");
			case EventType.FOLDER_RELOCATION:
				return $("ListEventsView.folderRelocation");
			case EventType.FOLDER_DEPOSIT:
				return $("ListEventsView.folderDeposit");
			case EventType.FOLDER_DESTRUCTION:
				return $("ListEventsView.folderDestruction");
			case EventType.PDF_A_GENERATION:
				return $("ListEventsView.pdfAGeneration");
			case EventType.RECEIVE_FOLDER:
				return $("ListEventsView.receiveFolder");
			case EventType.RECEIVE_CONTAINER:
				return $("ListEventsView.receiveContainer");
			case EventType.CURRENTLY_BORROWED_FOLDERS:
				return $("ListEventsView.currentlyBorrowedFolders");
			case EventType.LATE_BORROWED_FOLDERS:
				return $("ListEventsView.lateBorrowedFolders");
			case EventType.CONSULTATION_FOLDER:
				return $("ListEventsView.consultationFolders");
			case EventType.CREATE_TASK:
				return $("ListEventsView.createTask");
			case EventType.MODIFY_TASK:
				return $("ListEventsView.modifyTask");
			case EventType.DELETE_TASK:
				return $("ListEventsView.deleteTask");
			case EventType.REINDEXING:
				return $("ListEventsView.reindexing");
			case EventType.RESTARTING:
				return $("ListEventsView.restarting");
			case EventType.BORROW_REQUEST_FOLDER:
				return $("ListEventsView.borrowRequestFolder");
			case EventType.RETURN_REQUEST_FOLDER:
				return $("ListEventsView.returnRequestFolder");
			case EventType.REACTIVATION_REQUEST_FOLDER:
				return $("ListEventsView.reactivationRequestFolder");
			case EventType.BORROW_EXTENSION_REQUEST_FOLDER:
				return $("ListEventsView.borrowExtensionRequestFolder");
			case EventType.BORROW_REQUEST_CONTAINER:
				return $("ListEventsView.borrowRequestContainer");
			case EventType.RETURN_REQUEST_CONTAINER:
				return $("ListEventsView.returnRequestContainer");
			case EventType.REACTIVATION_REQUEST_CONTAINER:
				return $("ListEventsView.reactivationRequestContainer");
			case EventType.BORROW_EXTENSION_REQUEST_CONTAINER:
				return $("ListEventsView.borrowExtensionRequestContainer");
			case EventType.OPEN_DOCUMENT:
				return $("ListEventsView.openDocument");
			case EventType.DOWNLOAD_DOCUMENT:
				return $("ListEventsView.downloadDocument");
			case EventType.UPLOAD_DOCUMENT:
				return $("ListEventsView.uploadDocument");
			case EventType.SHARE_DOCUMENT:
				return $("ListEventsView.shareDocument");
			case EventType.FINALIZE_DOCUMENT:
				return $("ListEventsView.finalizeDocument");
			case EventType.SHARE_FOLDER:
				return $("ListEventsView.shareFolder");
			default:
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
		return metadataCodes;
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
			eventType.contains(EventType.MODIFY_PERMISSION)) {
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
