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
package com.constellio.model.extensions;

import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.data.frameworks.extensions.VaultEventListenerList;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordRestorationEvent;

public class ModelLayerCollectionEventsListeners {

	public VaultEventListenerList<RecordCreationEvent> recordsCreationListeners = new VaultEventListenerList<>();

	public VaultEventListenerList<RecordModificationEvent> recordsModificationListeners = new VaultEventListenerList<>();

	public VaultEventListenerList<RecordLogicalDeletionEvent> recordsLogicallyDeletionListeners = new VaultEventListenerList<>();

	public VaultEventListenerList<RecordPhysicalDeletionEvent> recordsPhysicallyDeletionListeners = new VaultEventListenerList<>();

	public VaultEventListenerList<RecordRestorationEvent> recordsRestorationListeners = new VaultEventListenerList<>();

	//	public VaultEventListenerList<NewAuthorizationOnUserEvent> newUserAuthorizationListener = new VaultEventListenerList<>();
	//
	//	public VaultEventListenerList<RemovedAuthorizationOnUserEvent> removedUserAuthorizationListener = new VaultEventListenerList<>();
	//
	//	public VaultEventListenerList<ModifiedAuthorizationOnUserEvent> modifiedUserAuthorizationListener = new VaultEventListenerList<>();

	public VaultBehaviorsList<RecordImportExtension> recordImportBehaviors = new VaultBehaviorsList<>();
}
