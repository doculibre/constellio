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
package com.constellio.app.ui.framework.data.event.category;

import org.joda.time.LocalDateTime;

import com.constellio.app.ui.pages.events.EventCategory;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.services.factories.ModelLayerFactory;

public class EventsListDataProviderFactory {

	public static EventsCategoryDataProvider getEventsListDataProviderFactory(EventCategory eventCategory, ModelLayerFactory modelLayerFactory, String collection , String currentUserName, LocalDateTime startDate, LocalDateTime endDate){
		return getEventsListDataProviderFactory(eventCategory, modelLayerFactory, collection, currentUserName, startDate, endDate, null);
	}

	public static EventsCategoryDataProvider getEventsListDataProviderFactory(EventCategory eventCategory, ModelLayerFactory modelLayerFactory, String collection , String currentUserName, String id) {
		return getEventsListDataProviderFactory(eventCategory, modelLayerFactory, collection, currentUserName, null, null, id);
	}

	public static EventsCategoryDataProvider getEventsListDataProviderFactory(EventCategory eventCategory, ModelLayerFactory modelLayerFactory, String collection , String currentUserName) {
		return getEventsListDataProviderFactory(eventCategory, modelLayerFactory, collection, currentUserName, null, null, null);
	}

	public static EventsCategoryDataProvider getEventsListDataProviderFactory(EventCategory eventCategory, ModelLayerFactory modelLayerFactory, String collection , String currentUserName, LocalDateTime startDate, LocalDateTime endDate, String id) {
		switch (eventCategory){
		case SYSTEM_USAGE: return new SystemUsageEventsDataProvider(modelLayerFactory, collection, currentUserName, startDate, endDate);
		case FOLDERS_AND_DOCUMENTS_CREATION: return new DocumentAndFoldersCreationEventsDataProvider(modelLayerFactory, collection, currentUserName, startDate, endDate);
		case FOLDERS_AND_DOCUMENTS_MODIFICATION: return new DocumentAndFoldersModificationEventsDataProvider(modelLayerFactory, collection, currentUserName, startDate, endDate);
		case FOLDERS_AND_DOCUMENTS_DELETION: return new DocumentAndFoldersDeletionEventsDataProvider(modelLayerFactory, collection, currentUserName, startDate, endDate);
		case CURRENTLY_BORROWED_DOCUMENTS:return new CurrentlyBorrowedDocumentsEventDataProvider(modelLayerFactory, collection, currentUserName);
		case CURRENTLY_BORROWED_FOLDERS:return new CurrentlyBorrowedFoldersEventDataProvider(modelLayerFactory, collection, currentUserName);
		case DOCUMENTS_BORROW_OR_RETURN:return new BorrowedOrReturnedDocumentsEventsDataProvider(modelLayerFactory, collection, currentUserName, startDate, endDate);
		case FOLDERS_BORROW_OR_RETURN:return new BorrowedOrReturnedFoldersEventsDataProvider(modelLayerFactory, collection, currentUserName, startDate, endDate);
		case CONTAINERS_BORROW_OR_RETURN:return new BorrowedOrReturnedContainersEventsDataProvider(modelLayerFactory, collection, currentUserName, startDate, endDate);
		case EVENTS_BY_ADMINISTRATIVE_UNIT:return new ByAdministrativeUnitEventsDataProvider(modelLayerFactory, collection, currentUserName, startDate, endDate, id);
		case EVENTS_BY_FOLDER: return new ByFolderEventsDataProvider(modelLayerFactory, collection, currentUserName, startDate, endDate, id);
		case EVENTS_BY_USER: return new ByUserEventsDataProvider(modelLayerFactory, collection, currentUserName, startDate, endDate, id);
		case CONNECTED_USERS_EVENT: return new ConnectedUsersEventDataProvider(modelLayerFactory, collection, currentUserName);
		case DECOMMISSIONING_EVENTS: return new DecommissioningEventsDataProvider(modelLayerFactory, collection, currentUserName, startDate, endDate);
		case USERS_AND_GROUPS_ADD_OR_REMOVE : return new UsersAndGroupsAddOrRemoveEventsDataProvider(modelLayerFactory, collection, currentUserName, startDate, endDate);
		default: throw new RuntimeException("Unsupported");
		}
	}
}
