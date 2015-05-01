/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.framework.data.event;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.pages.events.EventCategory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;

public class EventTypeUtils implements Serializable {
	
	public static String getEventTypeCaption(String eventType) {
		if (eventType.equals(EventType.OPEN_SESSION)) {
			return $("ListEventsView.openedSessions");
		} else if (eventType.equals(EventType.VIEW_FOLDER)) {
			return $("ListEventsView.foldersView");
		} else if (eventType.equals(EventType.CREATE_FOLDER)) {
			return $("ListEventsView.foldersCreation");
		} else if (eventType.equals(EventType.MODIFY_FOLDER)) {
			return $("ListEventsView.foldersModification");
		} else if (eventType.equals(EventType.DELETE_FOLDER)) {
			return $("ListEventsView.foldersDeletion");
		} else if (eventType.equals(EventType.BORROW_FOLDER)) {
			return $("ListEventsView.borrowedFolders");
		}else if (eventType.equals(EventType.RETURN_FOLDER)) {
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
		}else if (eventType.equals(EventType.BORROW_DOCUMENT)) {
			return $("ListEventsView.borrowedDocuments");
		}else if (eventType.equals(EventType.RETURN_DOCUMENT)) {
			return $("ListEventsView.returnedDocuments");
		}  else if (eventType.equals(EventType.BORROW_CONTAINER)) {
			return $("ListEventsView.borrowedContainers");
		}else if (eventType.equals(EventType.RETURN_CONTAINER)) {
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
		} else if (eventType.equals(EventType.MODIFY_PERMISSION_FOLDER)) {
			return $("ListEventsView.modifiedPermissions.folder");
		} else if (eventType.equals(EventType.DELETE_PERMISSION_FOLDER)) {
			return $("ListEventsView.deletedPermissions.folder");
		} else if (eventType.equals(EventType.GRANT_PERMISSION_DOCUMENT)) {
			return $("ListEventsView.grantedPermissions.document");
		} else if (eventType.equals(EventType.MODIFY_PERMISSION_DOCUMENT)) {
			return $("ListEventsView.modifiedPermissions.document");
		}else if (eventType.equals(EventType.DELETE_PERMISSION_DOCUMENT)){
			return $("ListEventsView.deletedPermissions.document");
		}  else if (eventType.equals(EventType.FOLDER_RELOCATION)){
			return $("ListEventsView.folderRelocation");
		}  else if (eventType.equals(EventType.FOLDER_DEPOSIT)) {
			return $("ListEventsView.folderDeposit");
		} else if (eventType.equals(EventType.FOLDER_DESTRUCTION)) {
			return $("ListEventsView.folderDestruction");
		} else if (eventType.equals(EventType.PDF_A_GENERATION)) {
			return $("ListEventsView.pdfAGeneration");
		} else if (eventType.equals(EventType.RECEIVE_FOLDER)){
			return $("ListEventsView.receiveFolder");
		}else if (eventType.equals(EventType.RECEIVE_CONTAINER)){
			return $("ListEventsView.receiveContainer");
		}else{
			throw new UnsupportedEventTypeRuntimeException(eventType);
		}
	}

	public static List<String> getDisplayedMetadataCodes(MetadataSchema metadataSchema, String eventType) {
		List<String> metadataCodes = new ArrayList<>();
		metadataCodes.addAll(getCommunMetadata(metadataSchema));
		metadataCodes.addAll(getSpecificMetadata(metadataSchema, eventType));
		return  metadataCodes;
	}

	private static List<String> getCommunMetadata(MetadataSchema metadataSchema) {
		List<String> metadataCodes = new ArrayList<>();
		Metadata eventUserNameMetadata = metadataSchema.getMetadata(Event.USERNAME);
		Metadata eventUserIPMetadata = metadataSchema.getMetadata(Event.IP);
		Metadata eventDateMetadata = metadataSchema.getMetadata(Schemas.CREATED_ON.getLocalCode());
		Metadata eventUserRolesMetadata = metadataSchema.getMetadata(Event.USER_ROLES);
		metadataCodes.add(eventUserNameMetadata.getCode());
		metadataCodes.add(eventDateMetadata.getCode());
		metadataCodes.add(eventUserRolesMetadata.getCode());
		metadataCodes.add(eventUserIPMetadata.getCode());
		return metadataCodes;
	}

	private static List<String> getSpecificMetadata(MetadataSchema metadataSchema,
			String eventType) {
		List<String> metadataCodes = new ArrayList<>();
		if(isPermissionEvent(eventType)){
			metadataCodes.addAll(getEventRecordMetadata(metadataSchema));
			metadataCodes.addAll(getEventPermissionMetadata(metadataSchema));
		}else if (isRecordEvent(eventType)){
			metadataCodes.addAll(getEventRecordMetadata(metadataSchema));
			if(eventType.equals(EventType.DELETE_FOLDER) || eventType.equals(EventType.DELETE_DOCUMENT)){
				Metadata reasonMetadata = metadataSchema.getMetadata(Event.REASON);
				metadataCodes.add(reasonMetadata.getCode());
			}
		} else if (isUserEvent(eventType)||
				 isGroupEvent(eventType)){
			metadataCodes.addAll(getEventUserMetadata(metadataSchema));
		}
		if(isModificationEvent(eventType)){
			metadataCodes.add(Event.DELTA);
		}
		return  metadataCodes;
	}

	private static List<String> getEventUserMetadata(MetadataSchema metadataSchema) {
		List<String> metadataCodes = new ArrayList<>();
		Metadata eventTitleMetadata = metadataSchema.getMetadata(Schemas.TITLE.getLocalCode());
		metadataCodes.add(eventTitleMetadata.getCode());
		return metadataCodes;
	}

	public static boolean isUserEvent(String eventType) {
		if(eventType.contains("_user")){
			return true;
		}else{
			return false;
		}
	}

	private static boolean isGroupEvent(String eventType) {
		if(eventType.contains("_group")){
			return true;
		}else{
			return false;
		}
	}

	public static boolean isRecordEvent(String eventType){
		return isFolderEvent(eventType)||
				isDocumentEvent(eventType)||
				isContainerEvent(eventType);
	}

	private static boolean isContainerEvent(String eventType) {
		//FIXME list all events
		if(eventType.contains("_container")){
			return true;
		}else{
			return false;
		}
	}

	private static boolean isDocumentEvent(String eventType) {
		//FIXME list all events
		if(eventType.contains("_document")){
			return true;
		}else{
			return false;
		}
	}

	private static boolean isFolderEvent(String eventType) {
		//FIXME list all events
		if(eventType.contains("_folder")){
			return true;
		}else{
			return false;
		}
	}

	private static boolean isPermissionEvent(String eventType) {
		if (eventType.contains(EventType.DELETE_PERMISSION)||
				eventType.contains(EventType.GRANT_PERMISSION)||
				eventType.contains(EventType.MODIFY_PERMISSION)){
			return true;
		}else{
			return false;
		}
	}

	private static List<String> getEventPermissionMetadata(MetadataSchema metadataSchema) {
		List<String> metadataCodes = new ArrayList<>();
		Metadata permissionDatesMetadata = metadataSchema.getMetadata(Event.PERMISSION_DATE_RANGE);
		Metadata permissionRolesMetadata = metadataSchema.getMetadata(Event.PERMISSION_ROLES);
		Metadata permissionUsersMetadata = metadataSchema.getMetadata(Event.PERMISSION_USERS);
		metadataCodes.add(permissionDatesMetadata.getCode());
		metadataCodes.add(permissionRolesMetadata.getCode());
		metadataCodes.add(permissionUsersMetadata.getCode());
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
		if (eventType.contains("modify_")){
			return true;
		}else{
			return false;
		}
	}
}
