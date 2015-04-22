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
package com.constellio.app.modules.rm.extensions;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.extensions.ModelLayerCollectionEventsListeners;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordCreationEvent.RecordCreationEventListener;
import com.constellio.model.extensions.events.records.RecordEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent.RecordModificationEventListener;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionEvent.RecordPhysicalDeletionEventListener;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.security.AuthorizationsServices;

public class AdministrativeUnitRecordSynchronization {

	String collection;

	ModelLayerFactory modelLayerFactory;

	RMSchemasRecordsServices rm;

	AuthorizationsServices authorizationsServices;

	public AdministrativeUnitRecordSynchronization(String collection, ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.authorizationsServices = modelLayerFactory.newAuthorizationsServices();
	}

	private Authorization getUsersAuthorization(String administrativeUnitId) {
		String authorizationId = "rw_U_ua" + administrativeUnitId.replace("_", "") + "users";
		return authorizationsServices.getAuthorization(collection, authorizationId);
	}

	private Authorization getAdministratorsAuthorization(String administrativeUnitId) {
		String authorizationId = "rwd_M_ua" + administrativeUnitId.replace("_", "") + "admins";
		return authorizationsServices.getAuthorization(collection, authorizationId);
	}

	private void syncUserAuthorization(AdministrativeUnit administrativeUnit) {
		Authorization authorization = getUsersAuthorization(administrativeUnit.getId());
		authorization.setGrantedToPrincipals(administrativeUnit.getFilingSpacesUsers());
		authorizationsServices.modify(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, User.GOD);
	}

	private void syncAdministratorAuthorization(AdministrativeUnit administrativeUnit) {
		Authorization authorization = getAdministratorsAuthorization(administrativeUnit.getId());
		authorization.setGrantedToPrincipals(administrativeUnit.getFilingSpacesAdministrators());
		authorizationsServices.modify(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, User.GOD);
	}

	private void sync(RecordModificationEvent event) {
		if (requiresUsersAuthorizationSync(event)) {
			syncUserAuthorization(rm.wrapAdministrativeUnit(event.getRecord()));
		}

		if (requiresAdministratorsAuthorizationSync(event)) {
			syncAdministratorAuthorization(rm.wrapAdministrativeUnit(event.getRecord()));
		}
	}

	private void createUserAuthorizations(AdministrativeUnit administrativeUnit) {
		String usersAuthorizationId = "ua" + administrativeUnit.getId().replace("_", "") + "users";
		List<String> userRoles = asList(Role.WRITE, RMRoles.USER);
		AuthorizationDetails details = AuthorizationDetails.createSynced(usersAuthorizationId, userRoles,
				administrativeUnit.getCollection());
		Authorization authorization = new Authorization();
		authorization.setDetail(details);
		authorization.setGrantedOnRecords(asList(administrativeUnit.getId()));
		authorization.setGrantedToPrincipals(administrativeUnit.getFilingSpacesUsers());
		authorizationsServices.add(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, User.GOD);
	}

	private void createAdministratorAuthorizations(AdministrativeUnit administrativeUnit) {
		String adminsAuthorizationId = "ua" + administrativeUnit.getId().replace("_", "") + "admins";
		List<String> userRoles = asList(Role.WRITE, Role.DELETE, RMRoles.MANAGER);
		AuthorizationDetails details = AuthorizationDetails.createSynced(adminsAuthorizationId, userRoles,
				administrativeUnit.getCollection());
		Authorization authorization = new Authorization();
		authorization.setDetail(details);
		authorization.setGrantedOnRecords(asList(administrativeUnit.getId()));
		authorization.setGrantedToPrincipals(administrativeUnit.getFilingSpacesAdministrators());
		authorizationsServices.add(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, User.GOD);
	}

	private void deleteAuthorizations(String administrativeUnitId) {
		authorizationsServices.delete(getUsersAuthorization(administrativeUnitId).getDetail(), User.GOD);
		authorizationsServices.delete(getAdministratorsAuthorization(administrativeUnitId).getDetail(), User.GOD);
	}

	private boolean requiresUsersAuthorizationSync(RecordModificationEvent event) {
		return event.hasModifiedMetadata(AdministrativeUnit.FILING_SPACES_USERS);
	}

	private boolean requiresAdministratorsAuthorizationSync(RecordModificationEvent event) {
		return event.hasModifiedMetadata(AdministrativeUnit.FILING_SPACES_ADMINISTRATORS);
	}

	private boolean isAdministrativeUnitRecord(RecordEvent event) {
		return event.getRecord().getSchemaCode().startsWith(AdministrativeUnit.SCHEMA_TYPE);
	}

	public void registerTo(ModelLayerCollectionEventsListeners listeners) {
		listeners.recordsCreationListeners.add(new RecordCreationEventListener() {

			public void notify(RecordCreationEvent event) {
				if (isAdministrativeUnitRecord(event)) {
					AdministrativeUnit administrativeUnit = rm.wrapAdministrativeUnit(event.getRecord());
					createUserAuthorizations(administrativeUnit);
					createAdministratorAuthorizations(administrativeUnit);
				}
			}
		});

		listeners.recordsModificationListeners.add(new RecordModificationEventListener() {

			public void notify(RecordModificationEvent event) {
				if (isAdministrativeUnitRecord(event)) {
					sync(event);
				}
			}
		});

		listeners.recordsPhysicallyDeletionListeners.add(new RecordPhysicalDeletionEventListener() {

			public void notify(RecordPhysicalDeletionEvent event) {
				if (isAdministrativeUnitRecord(event)) {
					deleteAuthorizations(event.getRecord().getId());
				}
			}
		});

	}

}
