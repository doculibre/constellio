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

import static com.constellio.app.modules.rm.wrappers.DecommissioningList.ADMINISTRATIVE_UNIT;
import static com.constellio.app.modules.rm.wrappers.DecommissioningList.PROCESSING_DATE;
import static com.constellio.app.modules.rm.wrappers.DecommissioningList.STATUS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.List;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;

public class DecomissioningListQueryFactory {
	RMSchemasRecordsServices rm;
	TaxonomiesSearchServices taxonomiesSearchServices;
	SearchServices searchServices;
	DecommissioningService decommissioningService;
	AuthorizationsServices authorizationsServices;

	public DecomissioningListQueryFactory(String collection, ModelLayerFactory modelLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.decommissioningService = new DecommissioningService(collection, modelLayerFactory);
		this.authorizationsServices = modelLayerFactory.newAuthorizationsServices();
	}

	public LogicalSearchQuery getProcessedListsQuery(User user) {

		MetadataSchema schema = rm.defaultDecommissioningListSchema();
		if (user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).onSomething()) {
			LogicalSearchCondition condition = from(schema).where(schema.getMetadata(PROCESSING_DATE)).isNotNull();
			return newQueryWithAdministrativeUnitFilter(condition, user);

		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getGeneratedListsQuery(User user) {

		MetadataSchema schema = rm.defaultDecommissioningListSchema();
		if (user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).onSomething()) {
			LogicalSearchCondition condition = from(schema).where(schema.getMetadata(PROCESSING_DATE)).isNull()
					.andWhere(schema.getMetadata(STATUS)).isNotEqual(DecomListStatus.IN_VALIDATION);
			return newQueryWithAdministrativeUnitFilter(condition, user);

		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getListsPendingValidationQuery(User user) {
		if (user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).onSomething()) {
			MetadataSchema schema = rm.defaultDecommissioningListSchema();
			LogicalSearchCondition condition = from(schema)
					.where(schema.getMetadata(STATUS)).isEqualTo(DecomListStatus.IN_VALIDATION)
					.andWhere(schema.getMetadata(DecommissioningList.PROCESSING_DATE)).isNull();
			return newQueryWithAdministrativeUnitFilter(condition, user);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	public LogicalSearchQuery getListsToValidateQuery(User user) {
		MetadataSchema schema = rm.defaultDecommissioningListSchema();
		LogicalSearchCondition condition = from(schema)
				.where(schema.getMetadata(DecommissioningList.PENDING_VALIDATIONS)).isEqualTo(user);
		return new LogicalSearchQuery(condition).sortAsc(Schemas.TITLE);
	}

	public LogicalSearchQuery getListsPendingApprovalQuery(User user) {
		if (user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).onSomething()) {
			MetadataSchema schema = rm.defaultDecommissioningListSchema();
			LogicalSearchCondition condition = from(schema)
					.where(schema.getMetadata(STATUS)).isEqualTo(DecomListStatus.IN_APPROVAL);
			return newQueryWithAdministrativeUnitFilter(condition, user);
		} else {
			return LogicalSearchQuery.returningNoResults();
		}
	}

	private LogicalSearchQuery newQueryWithAdministrativeUnitFilter(LogicalSearchCondition condition, User user) {
		if (user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).globally()) {
			return new LogicalSearchQuery(condition).sortAsc(Schemas.TITLE);
		} else {
			MetadataSchema schema = rm.defaultDecommissioningListSchema();
			List<String> administrativeUnits = authorizationsServices.getConceptsForWhichUserHasPermission(
					RMPermissionsTo.MANAGE_DECOMMISSIONING, user);
			return new LogicalSearchQuery().sortAsc(Schemas.TITLE)
					.setCondition(condition.andWhere(schema.getMetadata(ADMINISTRATIVE_UNIT)).isIn(administrativeUnits));
		}
	}
}
