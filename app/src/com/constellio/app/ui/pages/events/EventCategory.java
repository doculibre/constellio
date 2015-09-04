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

public enum EventCategory {
	SYSTEM_USAGE,
	USERS_AND_GROUPS_ADD_OR_REMOVE,
	FOLDERS_AND_DOCUMENTS_CREATION,
	FOLDERS_AND_DOCUMENTS_MODIFICATION,
	FOLDERS_AND_DOCUMENTS_DELETION,
	CURRENTLY_BORROWED_DOCUMENTS,
	CURRENTLY_BORROWED_FOLDERS,
	DOCUMENTS_BORROW_OR_RETURN,
	FOLDERS_BORROW_OR_RETURN,
	CONTAINERS_BORROW_OR_RETURN,
	EVENTS_BY_ADMINISTRATIVE_UNIT,
	EVENTS_BY_FOLDER,
	EVENTS_BY_USER,
	CONNECTED_USERS_EVENT,
	DECOMMISSIONING_EVENTS,
	AGENT_EVENTS,
	TASKS_EVENTS
}
