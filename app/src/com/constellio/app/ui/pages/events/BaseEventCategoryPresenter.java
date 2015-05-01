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
package com.constellio.app.ui.pages.events;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.app.ui.framework.data.event.category.EventsListDataProviderFactory;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

public class BaseEventCategoryPresenter extends BasePresenter<BaseEventCategoryView> {

	public BaseEventCategoryPresenter(BaseEventCategoryView view) {
		super(view);
	}

	public void displayEvent(Integer itemId, EventCategory eventCategory) {
		Map<String, Object> eventParameters = new HashMap<>();

		String eventType = getEventListDataProvider(eventCategory).getEventType(itemId);
		eventParameters.put(EventViewParameters.EVENT_TYPE, eventType);
		eventParameters.put(EventViewParameters.EVENT_CATEGORY, eventCategory);
		if (view.getEventId() != null) {
			eventParameters.put(EventViewParameters.BY_ID_EVENT_PARAMETER, view.getEventId());
		}
		if (view.getEventStartDate() != null) {
			eventParameters.put(EventViewParameters.EVENT_START_DATE, LocalDateTime.fromDateFields(view.getEventStartDate()));
		}
		if (view.getEventEndDate() != null) {
			eventParameters.put(EventViewParameters.EVENT_END_DATE, LocalDateTime.fromDateFields(view.getEventEndDate()));
		}
		/*switch (eventCategory){
		case CURRENTLY_BORROWED_FOLDERS:
		case CURRENTLY_BORROWED_DOCUMENTS:
		case CONNECTED_USERS_EVENT : {
			//TODO create appropriate view
			return;
		}
		}*/

		view.navigateTo().showEvent(eventParameters);
	}

	public EventsCategoryDataProvider getEventListDataProvider(EventCategory eventCategory) {
		String collection = view.getCollection();
		User currentUser = getCurrentUser();
		String username = currentUser.getUsername();
		LocalDateTime startDate = (view.getEventStartDate() == null) ?
				null :
				LocalDateTime.fromDateFields(view.getEventStartDate());
		LocalDateTime endDate = (view.getEventEndDate() == null) ? null : LocalDateTime.fromDateFields(view.getEventEndDate());
		return EventsListDataProviderFactory
				.getEventsListDataProviderFactory(eventCategory, modelLayerFactory, collection, username,
						startDate, endDate, view.getEventId());
	}

	public String getReportTitle(EventCategory eventCategory) {
		return getEventListDataProvider(eventCategory).getDataReportTitle();
	}

	public boolean isWithReportPanel(EventCategory eventCategory) {
		/*switch (eventCategory){
		case DECOMMISSIONING_EVENTS:
		case CONTAINERS_BORROW_OR_RETURN:
		case DOCUMENTS_BORROW_OR_RETURN:
		case FOLDERS_BORROW_OR_RETURN:
		case CONNECTED_USERS_EVENT:
		case EVENTS_BY_ADMINISTRATIVE_UNIT: return false;
		default: return true;
		}*/
		return false;
	}

	public boolean isByRangeDate(EventCategory eventCategory) {
		switch (eventCategory) {
		case CURRENTLY_BORROWED_FOLDERS:
		case CURRENTLY_BORROWED_DOCUMENTS:
		case CONNECTED_USERS_EVENT:
			return false;
		default:
			return true;
		}
	}

	public boolean hasFetchById(EventCategory eventCategory) {
		switch (eventCategory) {
		case EVENTS_BY_ADMINISTRATIVE_UNIT:
		case EVENTS_BY_FOLDER:
		case EVENTS_BY_USER:
			return true;
		default:
			return false;
		}
	}

	public String getTitle(EventCategory eventCategory) {
		EventsCategoryDataProvider eventProvider = getEventListDataProvider(eventCategory);
		return eventProvider.getDataTitle();
	}

	public void listEvents() {
		view.navigateTo().listEvents();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.VIEW_EVENTS).globally();
	}
}
