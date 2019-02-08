package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningMainPresenter;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class DecommissioningSecurityService {
	RMSchemasRecordsServices rm;
	TaxonomiesSearchServices taxonomiesSearchServices;
	SearchServices searchServices;
	DecommissioningService decommissioningService;

	public DecommissioningSecurityService(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.taxonomiesSearchServices = appLayerFactory.getModelLayerFactory().newTaxonomiesSearchService();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.decommissioningService = new DecommissioningService(collection, appLayerFactory);
	}

	public boolean hasAccessToDecommissioningMainPage(User user) {
		if (user.hasAny(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST, RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST, RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething()
			|| user.hasAny(RMPermissionsTo.CREATE_TRANSFER_DECOMMISSIONING_LIST, RMPermissionsTo.EDIT_TRANSFER_DECOMMISSIONING_LIST).onSomething()) {
			return true;
		}
		return searchServices.hasResults(
				from(rm.decommissioningList.schemaType()).where(rm.decommissioningList.pendingValidations()).isEqualTo(user));
	}

	public boolean hasAccessToDecommissioningListPage(DecommissioningList list, User user) {
		return hasProcessPermissionOnList(user, list) || hasCreatePermissionOnList(user, list)
				|| hasManageDecommissioningPermissionOnList(user, list) ||
			   hasPermissionToCreateTransferOnList(list, user) ||
			   canValidate(list, user);
	}

	public boolean hasCreatePermissionOnList(User user, DecommissioningList list) {
		return hasPermissionOnList(user, list, RMPermissionsTo.CREATE_DECOMMISSIONING_LIST);
	}

	public boolean hasProcessPermissionOnList(User user, DecommissioningList list) {
		return hasPermissionOnList(user, list, RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST);
	}

	private boolean hasManageDecommissioningPermissionOnList(User user, DecommissioningList list) {
		return hasPermissionOnList(user, list, RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST);
	}

	private boolean hasEditPermissionOnList(User user, DecommissioningList list) {
		return hasPermissionOnList(user, list, RMPermissionsTo.EDIT_DECOMMISSIONING_LIST) ||
			   hasPermissionToEditTransferOnList(list, user);
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
		return hasProcessPermissionOnList(user, list) || hasCreatePermissionOnList(user, list) || hasPermissionToCreateTransferOnList(list, user) || hasManageDecommissioningPermissionOnList(user, list);
	}

	public boolean canCreateLists(User user) {
		return user.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething() ||
			   user.has(RMPermissionsTo.CREATE_TRANSFER_DECOMMISSIONING_LIST).onSomething();
	}

	public boolean canAskApproval(DecommissioningList list, User user) {
		return hasProcessPermissionOnList(user, list) || hasCreatePermissionOnList(user, list) || hasPermissionToCreateTransferOnList(list, user);
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
		List<String> tabs;
		boolean createDecommissioningListPerm = user.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething();
		if (user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething()
				|| createDecommissioningListPerm) {
			tabs = new ArrayList<>(Arrays.asList(
					DecommissioningMainPresenter.GENERATED,
					DecommissioningMainPresenter.PENDING_VALIDATION,
					DecommissioningMainPresenter.TO_VALIDATE,
					DecommissioningMainPresenter.VALIDATED,
					DecommissioningMainPresenter.PENDING_APPROVAL,
					DecommissioningMainPresenter.TO_APPROVE,
					DecommissioningMainPresenter.APPROVED,
					DecommissioningMainPresenter.PROCESSED));

			if(createDecommissioningListPerm) {
				tabs.add(0,	DecommissioningMainPresenter.CREATE);
			}
		} else if (user.has(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST).onSomething()) {
			tabs = new ArrayList<>(Arrays.asList(
					DecommissioningMainPresenter.PENDING_VALIDATION,
					DecommissioningMainPresenter.TO_VALIDATE,
					DecommissioningMainPresenter.VALIDATED,
					DecommissioningMainPresenter.TO_APPROVE));
		} else {
			tabs = new ArrayList<>(Arrays.asList(DecommissioningMainPresenter.TO_VALIDATE));
		}

		if (!tabs.contains(DecommissioningMainPresenter.CREATE) && user.has(RMPermissionsTo.CREATE_TRANSFER_DECOMMISSIONING_LIST).onSomething()) {
			tabs.add(0, DecommissioningMainPresenter.CREATE);
		}
		if (!tabs.contains(DecommissioningMainPresenter.GENERATED) && user.has(RMPermissionsTo.EDIT_TRANSFER_DECOMMISSIONING_LIST).onSomething()) {
			if (tabs.contains(DecommissioningMainPresenter.CREATE)) {
				tabs.add(1, DecommissioningMainPresenter.GENERATED);
			} else {
				tabs.add(0, DecommissioningMainPresenter.GENERATED);
			}
		}
		return tabs;
	}

	public boolean canCreateContainers(User user) {
		return user.has(RMPermissionsTo.MANAGE_CONTAINERS).onSomething();
	}

	public boolean hasAccessToManageContainersPage(User user) {
		return user.has(RMPermissionsTo.MANAGE_CONTAINERS).onSomething();
	}

	public boolean hasPermissionToCreateTransferOnList(DecommissioningList list, User user) {
		if (isListOfSearchTypeTransfer(list) && list.hasAnalogicalMedium() && !list.hasElectronicMedium()) {
			return user.has(RMPermissionsTo.CREATE_TRANSFER_DECOMMISSIONING_LIST).onSomething();
		}
		return false;
	}

	public boolean hasPermissionToEditTransferOnList(DecommissioningList list, User user) {
		if (isListOfSearchTypeTransfer(list) && list.hasAnalogicalMedium() && !list.hasElectronicMedium()) {
			return user.has(RMPermissionsTo.EDIT_TRANSFER_DECOMMISSIONING_LIST).onSomething();
		}
		return false;
	}

	public boolean isListOfSearchTypeTransfer(DecommissioningList list) {
		return DecommissioningListType.FOLDERS_TO_TRANSFER.equals(list.getDecommissioningListType())
			   && OriginStatus.ACTIVE.equals(list.getOriginArchivisticStatus());
	}
}
