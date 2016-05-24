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
		if (user.hasAny(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST, RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething()) {
			return true;
		}
		return searchServices.hasResults(
				from(rm.decommissioningList.schemaType()).where(rm.decommissioningList.pendingValidations()).isEqualTo(user));
	}

	public boolean hasAccessToDecommissioningListPage(DecommissioningList list, User user) {
		return hasProcessPermissionOnList(user, list) || hasManageDecommissioningPermissionOnList(user, list) ||
				canValidate(list, user);
	}

	private boolean hasProcessPermissionOnList(User user, DecommissioningList list) {
		return hasPermissionOnList(user, list, RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST);
	}

	private boolean hasManageDecommissioningPermissionOnList(User user, DecommissioningList list) {
		return hasPermissionOnList(user, list, RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST);
	}

	private boolean hasEditPermissionOnList(User user, DecommissioningList list) {
		return hasPermissionOnList(user, list, RMPermissionsTo.EDIT_DECOMMISSIONING_LIST);
	}

	private boolean hasPermissionOnList(User user, DecommissioningList list, String permission) {
		AdministrativeUnit administrativeUnit = rm.getAdministrativeUnit(list.getAdministrativeUnit());
		return user.has(permission).on(administrativeUnit);
	}

	public boolean canValidate(DecommissioningList list, User user) {
		DecomListValidation validation = list.getValidationFor(user.getId());
		return validation != null && !validation.isValidated();
	}

	public boolean canAskValidation(DecommissioningList list, User user) {
		return hasProcessPermissionOnList(user, list) || hasManageDecommissioningPermissionOnList(user, list);
	}

	public boolean canCreateLists(User user) {
		return user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething();
	}

	public boolean canAskApproval(DecommissioningList list, User user) {
		return hasProcessPermissionOnList(user, list);
	}

	public boolean canApprove(DecommissioningList list, User user) {
		return hasManageDecommissioningPermissionOnList(user, list);
	}

	public boolean canModify(DecommissioningList list, User user) {
		return hasEditPermissionOnList(user, list) && list.isUnprocessed();
	}

	public boolean canDelete(DecommissioningList list, User user) {
		return hasProcessPermissionOnList(user, list) && list.isUnprocessed();
	}

	public boolean canProcess(DecommissioningList list, User user) {
		return hasProcessPermissionOnList(user, list);
	}

	public boolean canModifyFoldersAndContainers(DecommissioningList list, User user) {
		return hasProcessPermissionOnList(user, list);
	}

	public List<String> getVisibleTabsInDecommissioningMainPage(User user) {
		if (user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething()) {
			return Arrays.asList(
					DecommissioningMainPresenter.CREATE,
					DecommissioningMainPresenter.GENERATED,
					DecommissioningMainPresenter.PENDING_VALIDATION,
					DecommissioningMainPresenter.TO_VALIDATE,
					DecommissioningMainPresenter.VALIDATED,
					DecommissioningMainPresenter.PENDING_APPROVAL,
					DecommissioningMainPresenter.TO_APPROVE,
					DecommissioningMainPresenter.APPROVED,
					DecommissioningMainPresenter.PROCESSED);
		} else if (user.has(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST).onSomething()) {
			return Arrays.asList(
					DecommissioningMainPresenter.PENDING_VALIDATION,
					DecommissioningMainPresenter.TO_VALIDATE,
					DecommissioningMainPresenter.VALIDATED,
					DecommissioningMainPresenter.TO_APPROVE);
		} else {
			return Arrays.asList(DecommissioningMainPresenter.TO_VALIDATE);
		}
	}
}
