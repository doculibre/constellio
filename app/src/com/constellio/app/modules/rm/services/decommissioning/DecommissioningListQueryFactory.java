package com.constellio.app.modules.rm.services.decommissioning;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;

public class DecommissioningListQueryFactory {
	RMSchemasRecordsServices rm;
	TaxonomiesSearchServices taxonomiesSearchServices;
	SearchServices searchServices;
	DecommissioningService decommissioningService;
	AuthorizationsServices authorizationsServices;

	public DecommissioningListQueryFactory(String collection, ModelLayerFactory modelLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.decommissioningService = new DecommissioningService(collection, modelLayerFactory);
		this.authorizationsServices = modelLayerFactory.newAuthorizationsServices();
	}

	public LogicalSearchQuery getGeneratedListsQuery(User user) {
		if (user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething()) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schemaType())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.GENERATED);
			return newQueryWithAdministrativeUnitFilter(condition, user);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getListsPendingValidationQuery(User user) {
		if (user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething()) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schema())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.IN_VALIDATION)
					.andWhere(rm.decommissioningList.approvalRequest()).isNull()
					.andWhere(rm.decommissioningList.pendingValidations()).isNotEqual(user);
			return newQueryWithAdministrativeUnitFilter(condition, user);
		} else if (user.has(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST).onSomething()) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schema())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.IN_VALIDATION)
					.andWhere(rm.decommissioningList.approvalRequest()).isNotNull()
					.andWhere(rm.decommissioningList.pendingValidations()).isNotEqual(user);
			return newQueryWithAdministrativeUnitFilter(condition, user, RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getListsToValidateQuery(User user) {
		LogicalSearchCondition condition = from(rm.decommissioningList.schema())
				.where(rm.decommissioningList.pendingValidations()).isEqualTo(user);
		return new LogicalSearchQuery(condition).sortAsc(Schemas.TITLE);
	}

	public LogicalSearchQuery getValidatedListsQuery(User user) {
		if (user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething()) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schema())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.VALIDATED);
			return newQueryWithAdministrativeUnitFilter(condition, user);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getListsPendingApprovalQuery(User user) {
		if (user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething()) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schema())
					.where(rm.decommissioningList.status()).isIn(
							Arrays.asList(DecomListStatus.IN_APPROVAL, DecomListStatus.IN_VALIDATION))
					.andWhere(rm.decommissioningList.approvalRequest()).isNotNull();
			return newQueryWithAdministrativeUnitFilter(condition, user);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getListsToApproveQuery(User user) {
		if (user.has(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST).onSomething()) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schema())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.IN_APPROVAL)
					.andWhere(rm.decommissioningList.approvalRequest()).isNotEqual(user);
			return newQueryWithAdministrativeUnitFilter(condition, user, RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getApprovedListsQuery(User user) {
		if (user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething()) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schema())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.APPROVED);
			return newQueryWithAdministrativeUnitFilter(condition, user);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getProcessedListsQuery(User user) {
		if (user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething()) {
			LogicalSearchCondition condition = from(rm.decommissioningList.schema())
					.where(rm.decommissioningList.status()).isEqualTo(DecomListStatus.PROCESSED);
			return newQueryWithAdministrativeUnitFilter(condition, user);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	private LogicalSearchQuery newQueryWithAdministrativeUnitFilter(LogicalSearchCondition condition, User user) {
		return newQueryWithAdministrativeUnitFilter(condition, user, RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST);
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
}
