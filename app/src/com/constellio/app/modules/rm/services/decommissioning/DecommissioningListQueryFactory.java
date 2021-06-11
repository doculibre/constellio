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

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
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
		LogicalSearchCondition superUserCondition = from(rm.decommissioningList.schemaType())
				.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.GENERATED)
				.andWhere(rm.decommissioningList.superUser()).isEqualTo(user.getId());

		if (hasProcessOrCreateDecomissioningPerm(user)) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.GENERATED);
			condition = conditionWithProcessAndCreateDecomissioningPermWithAdministrativeUnitFilter(condition, user);

			return wrapQuery(from(rm.decommissioningList.schemaType()).whereAnyCondition(condition, superUserCondition));
		} else {
			return wrapQuery(superUserCondition);
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

			LogicalSearchCondition superUserCondition = from(rm.decommissioningList.schemaType())
					.whereAllConditions(condition, where(rm.decommissioningList.superUser()).isEqualTo(user.getId()));

			condition = conditionWithAdministrativeUnitFilter(condition, user,
					asList(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST, RMPermissionsTo.CREATE_DECOMMISSIONING_LIST,
							RMPermissionsTo.EDIT_TRANSFER_DECOMMISSIONING_LIST));

			return wrapQuery(from(rm.decommissioningList.schemaType()).whereAnyCondition(condition, superUserCondition));
		} else {
			return getGeneratedListsQuery(user);
		}
	}

	public LogicalSearchQuery getListsPendingValidationQuery(User user) {
		LogicalSearchCondition superUserCondition = from(rm.decommissioningList.schemaType())
				.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.IN_VALIDATION)
				.andWhere(rm.decommissioningList.superUser()).isEqualTo(user.getId());

		if (hasProcessOrCreateDecomissioningPerm(user) ||
			user.has(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST).onSomething()) {

			boolean hasGlobalAdminUnitsProcessOrCreateDecomlist = false;
			List<String> adminUnitsProcessOrCreateDecomlist = new ArrayList<>();
			for (String permission : asList(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST, RMPermissionsTo.CREATE_DECOMMISSIONING_LIST)) {
				if (user.hasAll(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST, RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).globally()) {
					hasGlobalAdminUnitsProcessOrCreateDecomlist = true;
				} else {
					adminUnitsProcessOrCreateDecomlist
							.addAll(authorizationsServices.getConceptsForWhichUserHasPermission(permission, user));
				}
			}

			boolean hasApproveDecomList = false;
			List<String> approveDecomList = new ArrayList<>();
			if (user.has(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST).globally()) {
				hasApproveDecomList = true;
			} else {
				approveDecomList =
						authorizationsServices.getConceptsForWhichUserHasPermission(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST, user);
			}

			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType()).whereAllConditions(
					where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.IN_VALIDATION),
					where(rm.decommissioningList.pendingValidations()).isNotEqual(user),

					anyConditions(
							hasGlobalAdminUnitsProcessOrCreateDecomlist ? (
									where(rm.decommissioningList.approvalRequest()).isNull()
							) : (
									allConditions(
											where(rm.decommissioningList.approvalRequest()).isNull(),
											where(rm.decommissioningList.administrativeUnit()).isIn(adminUnitsProcessOrCreateDecomlist)
									)
							),
							hasApproveDecomList ? (
									where(rm.decommissioningList.approvalRequest()).isNotNull()
							) : (
									allConditions(
											where(rm.decommissioningList.approvalRequest()).isNotNull(),
											where(rm.decommissioningList.administrativeUnit()).isIn(approveDecomList)
									)
							)
					)
			);

			return wrapQuery(from(rm.decommissioningList.schemaType()).whereAnyCondition(condition, superUserCondition));
		} else {
			return wrapQuery(superUserCondition);
		}
	}

	private boolean hasProcessOrCreateDecomissioningPerm(User user) {
		return user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething()
			   || user.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething();
	}

	public LogicalSearchQuery getListsToValidateQuery(User user) {
		LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
				.where(rm.decommissioningList.pendingValidations()).isEqualTo(user);

		return wrapQuery(condition);
	}

	public LogicalSearchQuery getValidatedListsQuery(User user) {
		LogicalSearchCondition superUserCondition = from(rm.decommissioningList.schemaType())
				.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.VALIDATED)
				.andWhere(rm.decommissioningList.superUser()).isEqualTo(user.getId());

		if (hasProcessOrCreateDecomissioningPerm(user)) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.VALIDATED);
			condition = conditionWithProcessAndCreateDecomissioningPermWithAdministrativeUnitFilter(condition, user);

			return wrapQuery(from(rm.decommissioningList.schemaType()).whereAnyCondition(condition, superUserCondition));
		} else {
			return wrapQuery(superUserCondition);
		}
	}

	public LogicalSearchQuery getListsPendingApprovalQuery(User user) {
		LogicalSearchCondition superUserCondition = from(rm.decommissioningList.schemaType())
				.where(rm.decommissioningList.status()).isIn(asList(DecomListStatus.IN_APPROVAL, DecomListStatus.IN_VALIDATION))
				.andWhere(rm.decommissioningList.approvalRequest()).isNotNull()
				.andWhere(rm.decommissioningList.superUser()).isEqualTo(user.getId());

		if (hasProcessOrCreateDecomissioningPerm(user)) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isIn(
							asList(DecomListStatus.IN_APPROVAL, DecomListStatus.IN_VALIDATION))
					.andWhere(rm.decommissioningList.approvalRequest()).isNotNull();
			condition = conditionWithProcessAndCreateDecomissioningPermWithAdministrativeUnitFilter(condition, user);

			return wrapQuery(from(rm.decommissioningList.schemaType()).whereAnyCondition(condition, superUserCondition));
		} else {
			return wrapQuery(superUserCondition);
		}
	}

	public LogicalSearchQuery getListsToApproveQuery(User user) {
		if (user.has(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST).onSomething()) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.IN_APPROVAL)
					.andWhere(rm.decommissioningList.approvalRequest()).isNotEqual(user);
			condition = conditionWithAdministrativeUnitFilter(condition, user, RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST);

			return wrapQuery(condition);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getApprovedListsQuery(User user) {
		LogicalSearchCondition superUserCondition = from(rm.decommissioningList.schemaType())
				.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.APPROVED)
				.andWhere(rm.decommissioningList.superUser()).isEqualTo(user.getId());

		if (hasProcessOrCreateDecomissioningPerm(user)) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.APPROVED);
			condition = conditionWithProcessAndCreateDecomissioningPermWithAdministrativeUnitFilter(condition, user);

			return wrapQuery(from(rm.decommissioningList.schemaType()).whereAnyCondition(condition, superUserCondition));
		} else {
			return wrapQuery(superUserCondition);
		}
	}

	public LogicalSearchQuery getProcessedListsQuery(User user) {
		LogicalSearchCondition superUserCondition = from(rm.decommissioningList.schemaType())
				.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.PROCESSED)
				.andWhere(rm.decommissioningList.superUser()).isEqualTo(user.getId());

		if (hasProcessOrCreateDecomissioningPerm(user)) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.PROCESSED);
			condition = conditionWithProcessAndCreateDecomissioningPermWithAdministrativeUnitFilter(condition, user);

			return wrapQuery(from(rm.decommissioningList.schemaType()).whereAnyCondition(condition, superUserCondition));
		} else {
			return wrapQuery(superUserCondition);
		}
	}

	private LogicalSearchCondition conditionWithAdministrativeUnitFilter(LogicalSearchCondition condition, User user) {
		return conditionWithAdministrativeUnitFilter(condition, user, RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST);
	}

	private LogicalSearchCondition conditionWithProcessAndCreateDecomissioningPermWithAdministrativeUnitFilter(
			LogicalSearchCondition condition, User user) {
		return conditionWithAdministrativeUnitFilter(condition, user, asList(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST, RMPermissionsTo.CREATE_DECOMMISSIONING_LIST));
	}

	private LogicalSearchCondition conditionWithAdministrativeUnitFilter(LogicalSearchCondition condition, User user,
																		 String permission) {
		if (user.has(permission).globally()) {
			return condition;
		} else {
			List<String> administrativeUnits = authorizationsServices.getConceptsForWhichUserHasPermission(permission, user);
			return condition.andWhere(rm.decommissioningList.administrativeUnit()).isIn(administrativeUnits);
		}
	}

	private LogicalSearchCondition conditionWithAdministrativeUnitFilter(LogicalSearchCondition condition, User user,
																		 List<String> permissionList) {
		if (user.hasAny(permissionList).globally()) {
			return condition;
		} else {
			Set<String> administrativeUnits = new HashSet<>();
			for (String permission : permissionList) {
				administrativeUnits.addAll(authorizationsServices.getConceptsForWhichUserHasPermission(permission, user));
			}
			return condition.andWhere(rm.decommissioningList.administrativeUnit())
					.isIn(new ArrayList<Object>(administrativeUnits));
		}
	}

	private LogicalSearchQuery wrapQuery(LogicalSearchCondition condition) {
		return new LogicalSearchQuery(condition).sortAsc(Schemas.TITLE);
	}
}
