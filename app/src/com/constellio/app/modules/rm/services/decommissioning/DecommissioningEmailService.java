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
package com.constellio.app.modules.rm.services.decommissioning;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningEmailServiceException.CannotFindManangerEmail;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;

public class DecommissioningEmailService {

	String collection;
	RecordServices recordServices;
	AuthorizationsServices authorizationsServices;

	public DecommissioningEmailService(String collection, ModelLayerFactory modelLayerFactory) {
		this.authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.collection = collection;
	}

	public List<User> getUsersWithEmailAddressAndDecommissioningPermissionInConcept(RecordWrapper recordWrapper) {
		return getUsersWithEmailAddressAndDecommissioningPermissionInConcept(recordWrapper.getWrappedRecord());
	}

	public List<User> getUsersWithEmailAddressAndDecommissioningPermissionInConcept(Record record) {
		List<User> returnedUsers = filterUserWithoutEmail(authorizationsServices
				.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(
						RMPermissionsTo.MANAGE_DECOMMISSIONING, record));

		if (returnedUsers.isEmpty()) {
			String parentId = record.getParentId();
			if (parentId == null) {
				return getUsersWithEmailAddressWithGlobalDecommissioningPermission();
			} else {
				Record parentRecord = recordServices.getDocumentById(parentId);
				return getUsersWithEmailAddressAndDecommissioningPermissionInConcept(parentRecord);
			}

		} else {
			return returnedUsers;
		}
	}

	private List<User> getUsersWithEmailAddressWithGlobalDecommissioningPermission() {

		List<User> users = filterUserWithoutEmail(authorizationsServices.getUsersWithGlobalPermissionInCollectionExcludingRoles(
				RMPermissionsTo.MANAGE_DECOMMISSIONING, collection, asList(RMRoles.RGD)));

		if (users.isEmpty()) {
			return filterUserWithoutEmail(authorizationsServices.getUsersWithGlobalPermissionInCollection(
					RMPermissionsTo.MANAGE_DECOMMISSIONING, collection));
		} else {
			return users;
		}
	}

	private List<User> filterUserWithoutEmail(List<User> users) {
		List<User> returnedUsers = new ArrayList<>();
		for (User user : users) {
			if (StringUtils.isNotBlank(user.getEmail())) {
				returnedUsers.add(user);
			}
		}
		return returnedUsers;
	}

	public List<User> getManagerEmailForList(DecommissioningList decommissioningList)
			throws DecommissioningEmailServiceException {

		Record record = recordServices.getDocumentById(decommissioningList.getId());
		List<User> users = getUsersWithEmailAddressAndDecommissioningPermissionInConcept(record);
		if (users.isEmpty()) {
			throw new CannotFindManangerEmail(decommissioningList);
		} else {
			return users;
		}

	}

}
