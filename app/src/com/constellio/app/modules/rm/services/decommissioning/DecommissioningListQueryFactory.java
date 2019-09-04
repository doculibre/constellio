package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

public class DecommissioningListQueryFactory {
	RMSchemasRecordsServices rm;
	TaxonomiesSearchServices taxonomiesSearchServices;
	SearchServices searchServices;
	DecommissioningService decommissioningService;
	AuthorizationsServices authorizationsServices;

	public DecommissioningListQueryFactory(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		this.taxonomiesSearchServices = appLayerFactory.getModelLayerFactory().newTaxonomiesSearchService();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.decommissioningService = new DecommissioningService(collection, appLayerFactory);
		this.authorizationsServices = appLayerFactory.getModelLayerFactory().newAuthorizationsServices();
	}

	public LogicalSearchQuery getGeneratedListsQuery(User user) {
		if (hasProcessOrCreateDecomissioningPerm(user)) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.GENERATED);
			return newQueryWithProcessAndCreateDecomissioningPermWithAdministrativeUnitFilter(condition, user);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getGeneratedTransferListsQuery(User user) {
		if (hasProcessOrCreateDecomissioningPerm(user)
				|| user.has(RMPermissionsTo.EDIT_TRANSFER_DECOMMISSIONING_LIST).globally()) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.whereAllConditions(
							where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.GENERATED),
							where(rm.decommissioningList.type()).isEqualTo(DecommissioningListType.FOLDERS_TO_TRANSFER),
							where(rm.decommissioningList.analogicalMedium()).isTrue(),
							where(rm.decommissioningList.electronicMedium()).isFalse()
					);
			return newQueryWithAdministrativeUnitFilter(condition, user, asList(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST,
					RMPermissionsTo.CREATE_DECOMMISSIONING_LIST,
					RMPermissionsTo.EDIT_TRANSFER_DECOMMISSIONING_LIST));
		} else {
			return getGeneratedListsQuery(user);
		}
	}

	public LogicalSearchQuery getListsPendingValidationQuery(User user) {
		if (hasProcessAndCreateDecomissioningPerm(user)) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.IN_VALIDATION)
					.andWhere(rm.decommissioningList.approvalRequest()).isNull()
					.andWhere(rm.decommissioningList.pendingValidations()).isNotEqual(user);
			return newQueryWithProcessAndCreateDecomissioningPermWithAdministrativeUnitFilter(condition, user);
		} else if (user.has(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST).onSomething()) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.IN_VALIDATION)
					.andWhere(rm.decommissioningList.approvalRequest()).isNotNull()
					.andWhere(rm.decommissioningList.pendingValidations()).isNotEqual(user);
			return newQueryWithAdministrativeUnitFilter(condition, user, RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	private boolean hasProcessOrCreateDecomissioningPerm(User user) {
		return user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething()
			   || user.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething();
	}

	private boolean hasProcessAndCreateDecomissioningPerm(User user) {
		return user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething()
			   && user.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething();
	}

	public LogicalSearchQuery getListsToValidateQuery(User user) {
		LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
				.where(rm.decommissioningList.pendingValidations()).isEqualTo(user);
		return new LogicalSearchQuery(condition).sortAsc(Schemas.TITLE);
	}

	public LogicalSearchQuery getValidatedListsQuery(User user) {
		if (hasProcessOrCreateDecomissioningPerm(user)) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.VALIDATED);
			return newQueryWithProcessAndCreateDecomissioningPermWithAdministrativeUnitFilter(condition, user);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getListsPendingApprovalQuery(User user) {
		if (hasProcessOrCreateDecomissioningPerm(user)) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isIn(
							asList(DecomListStatus.IN_APPROVAL, DecomListStatus.IN_VALIDATION))
					.andWhere(rm.decommissioningList.approvalRequest()).isNotNull();
			return newQueryWithProcessAndCreateDecomissioningPermWithAdministrativeUnitFilter(condition, user);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getListsToApproveQuery(User user) {
		if (user.has(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST).onSomething()) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.IN_APPROVAL)
					.andWhere(rm.decommissioningList.approvalRequest()).isNotEqual(user);
			return newQueryWithAdministrativeUnitFilter(condition, user, RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getApprovedListsQuery(User user) {
		if (hasProcessOrCreateDecomissioningPerm(user)) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.APPROVED);
			return newQueryWithProcessAndCreateDecomissioningPermWithAdministrativeUnitFilter(condition, user);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getProcessedListsQuery(User user) {
		if (hasProcessOrCreateDecomissioningPerm(user)) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.PROCESSED);
			return newQueryWithProcessAndCreateDecomissioningPermWithAdministrativeUnitFilter(condition, user);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	private LogicalSearchQuery newQueryWithAdministrativeUnitFilter(LogicalSearchCondition condition, User user) {
		return newQueryWithAdministrativeUnitFilter(condition, user, RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST);
	}

	private LogicalSearchQuery newQueryWithProcessAndCreateDecomissioningPermWithAdministrativeUnitFilter(LogicalSearchCondition condition, User user) {
		return newQueryWithAdministrativeUnitFilter(condition, user, asList(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST, RMPermissionsTo.CREATE_DECOMMISSIONING_LIST));
	}


	private LogicalSearchQuery newQueryWithAdministrativeUnitFilter(
			LogicalSearchCondition condition, User user, String permission) {
		if (user.has(permission).globally()) {
			return new LogicalSearchQuery(condition).sortAsc(Schemas.TITLE);
		} else {
			List<String> administrativeUnits = authorizationsServices.getConceptsForWhichUserHasPermission(permission, user);
			return new LogicalSearchQuery(condition.andWhere(rm.decommissioningList.administrativeUnit()).isIn(administrativeUnits))
					.sortAsc(Schemas.TITLE);
		}
	}

	private LogicalSearchQuery newQueryWithAdministrativeUnitFilter(
			LogicalSearchCondition condition, User user, List<String> permissionList) {
		if (user.hasAny(permissionList).globally()) {
			return new LogicalSearchQuery(condition).sortAsc(Schemas.TITLE);
		} else {
			Set<String> administrativeUnits = new HashSet<>();
			for (String permission : permissionList) {
				administrativeUnits.addAll(authorizationsServices.getConceptsForWhichUserHasPermission(permission, user));
			}
			return new LogicalSearchQuery(condition.andWhere(rm.decommissioningList.administrativeUnit()).isIn(new ArrayList<Object>(administrativeUnits)))
					.sortAsc(Schemas.TITLE);
		}
	}
}
