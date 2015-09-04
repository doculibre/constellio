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

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningMainPresenter;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;

public class DecommissioningSecurityService {
	RMSchemasRecordsServices rm;
	TaxonomiesSearchServices taxonomiesSearchServices;
	SearchServices searchServices;
	DecommissioningService decommissioningService;

	public DecommissioningSecurityService(String collection, ModelLayerFactory modelLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.decommissioningService = new DecommissioningService(collection, modelLayerFactory);
	}

	public boolean hasAccessToDecommissioningMainPage(User user) {
		if (user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).onSomething()) {
			return true;
		}
		return searchServices.hasResults(from(rm.decommissioningListSchemaType())
				.where(rm.decommissioningListPendingValidations()).isEqualTo(user)
						// TODO Tom Decommissioning: This does not seem correct
				.orWhere(rm.decommissioningListApprovalUser()).isEqualTo(user));
	}

	public boolean hasAccessToDecommissioningListPage(DecommissioningList list, User user) {
		AdministrativeUnit administrativeUnit = rm.getAdministrativeUnit(list.getAdministrativeUnit());
		return user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).on(administrativeUnit) || canValidate(list, user);
	}

	private boolean hasManageDecommissioningPermissionOnList(DecommissioningList list, User user) {
		AdministrativeUnit administrativeUnit = rm.getAdministrativeUnit(list.getAdministrativeUnit());
		return user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).on(administrativeUnit);
	}

	public boolean canValidate(DecommissioningList list, User user) {
		DecomListValidation validation = list.getValidationFor(user.getId());
		return validation != null && !validation.isValidated();
	}

	public boolean canAskValidation(DecommissioningList list, User user) {
		return hasManageDecommissioningPermissionOnList(list, user);
	}

	public boolean canCreateLists(User user) {
		return user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).globally();
	}

	public boolean canAskApproval(DecommissioningList list, User user) {
		return hasManageDecommissioningPermissionOnList(list, user);
	}

	public boolean canApprove(DecommissioningList list, User user) {
		// TODO Tom Decommissioning: This does not seem correct
		return hasManageDecommissioningPermissionOnList(list, user);
	}

	public boolean canModify(DecommissioningList list, User user) {
		return hasManageDecommissioningPermissionOnList(list, user);
	}

	public boolean canDelete(DecommissioningList list, User user) {
		return hasManageDecommissioningPermissionOnList(list, user);
	}

	public boolean canProcess(DecommissioningList list, User user) {
		return hasManageDecommissioningPermissionOnList(list, user);
	}

	public boolean canModifyFoldersAndContainers(DecommissioningList list, User user) {
		return hasManageDecommissioningPermissionOnList(list, user);
	}

	public List<String> getVisibleTabsInDecommissioningMainPage(User user) {
		if (user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).onSomething()) {
			return Arrays.asList(
					DecommissioningMainPresenter.CREATE,
					DecommissioningMainPresenter.GENERATED,
					DecommissioningMainPresenter.PENDING_VALIDATION,
					DecommissioningMainPresenter.TO_VALIDATE,
					DecommissioningMainPresenter.PENDING_APPROVAL,
					DecommissioningMainPresenter.PROCESSED);
		} else {
			return Arrays.asList(DecommissioningMainPresenter.TO_VALIDATE);
		}
	}
}
