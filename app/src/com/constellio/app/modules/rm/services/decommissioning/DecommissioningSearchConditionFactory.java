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

import static com.constellio.app.modules.rm.model.enums.FolderStatus.ACTIVE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.SEMI_ACTIVE;
import static com.constellio.data.utils.TimeProvider.getLocalDate;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.isNotNull;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.isNull;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.model.enums.RetentionType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;

public class DecommissioningSearchConditionFactory {
	RMSchemasRecordsServices schemas;
	TaxonomiesSearchServices taxonomiesSearchServices;
	SearchServices searchServices;
	DecommissioningService decommissioningService;

	public DecommissioningSearchConditionFactory(String collection, ModelLayerFactory modelLayerFactory) {
		this.schemas = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.decommissioningService = new DecommissioningService(collection, modelLayerFactory);
	}

	public static List<SearchType> availableCriteriaForFoldersWithoutPlanifiedDate() {
		return Arrays.asList(SearchType.fixedPeriod, SearchType.code888, SearchType.code999);
	}

	public static List<SearchType> availableCriteriaForFoldersWithPlanifiedDate() {
		return Arrays.asList(SearchType.transfer, SearchType.activeToDeposit, SearchType.activeToDestroy,
				SearchType.semiActiveToDeposit, SearchType.semiActiveToDestroy);
	}

	public LogicalSearchCondition bySearchType(SearchType type, String adminUnitId) {
		switch (type) {
		case fixedPeriod:
			return withoutClosingDateAndWithFixedPeriod(adminUnitId);
		case code888:
			return withoutClosingDateAndWith888Period(adminUnitId);
		case code999:
			return withoutClosingDateAndWith999Period(adminUnitId);
		case transfer:
			return activeToTransferToSemiActive(adminUnitId);
		case activeToDeposit:
			return activeToDeposit(adminUnitId);
		case activeToDestroy:
			return activeToDestroy(adminUnitId);
		case semiActiveToDeposit:
			return semiActiveToDeposit(adminUnitId);
		case semiActiveToDestroy:
			return semiActiveToDestroy(adminUnitId);
		default:
			throw new RuntimeException("Unknown search type: " + type);
		}
	}

	public LogicalSearchCondition withoutClosingDateAndWithFixedPeriod(String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(adminUnitId)
				.andWhere(schemas.folderActiveRetentionType()).isEqualTo(RetentionType.FIXED)
				.andWhere(schemas.folderCloseDate()).isNull()
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(ACTIVE);
	}

	public LogicalSearchCondition withoutClosingDateAndWith888Period(String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(adminUnitId)
				.andWhere(schemas.folderActiveRetentionType()).isEqualTo(RetentionType.OPEN)
				.andWhere(schemas.folderCloseDate()).isNull()
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(ACTIVE);
	}

	public LogicalSearchCondition withoutClosingDateAndWith999Period(String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(adminUnitId)
				.andWhere(schemas.folderActiveRetentionType()).isEqualTo(RetentionType.UNTIL_REPLACED)
				.andWhere(schemas.folderCloseDate()).isNull()
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(ACTIVE);
	}

	public LogicalSearchCondition activeToTransferToSemiActive(String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(adminUnitId)
				.andWhere(schemas.folderPlanifiedTransferDate()).isLessOrEqualThan(getLocalDate())
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(ACTIVE);
	}

	public LogicalSearchCondition activeToDestroy(String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(adminUnitId)
				.andWhere(schemas.folderPlanifiedDestructionDate()).isLessOrEqualThan(getLocalDate())
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(ACTIVE);
	}

	public LogicalSearchCondition activeToDeposit(String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(adminUnitId)
				.andWhere(schemas.folderPlanifiedDepositDate()).isLessOrEqualThan(getLocalDate())
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(ACTIVE);
	}

	public LogicalSearchCondition semiActiveToDestroy(String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(adminUnitId)
				.andWhere(schemas.folderPlanifiedDestructionDate()).isLessOrEqualThan(getLocalDate())
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(SEMI_ACTIVE);
	}

	public LogicalSearchCondition semiActiveToDeposit(String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(adminUnitId)
				.andWhere(schemas.folderPlanifiedDepositDate()).isLessOrEqualThan(getLocalDate())
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(SEMI_ACTIVE);
	}

	public LogicalSearchCondition getVisibleContainersCondition(ContainerSearchParameters params) {
		params.validate();

		return from(schemas.containerRecordSchemaType())
				.where(schemas.containerAdministrativeUnit()).isEqualTo(params.adminUnitId)
				.andWhere(schemas.containerDecommissioningType()).isEqualTo(params.type)
				.andWhere(schemas.containerStorageSpace()).is(params.withStorage ? isNotNull() : isNull());
	}

	public long getVisibleContainersCount(ContainerSearchParameters params) {
		params.validate();

		List<String> units = decommissioningService.getAllAdminUnitIdsHierarchyOf(params.adminUnitId);

		LogicalSearchCondition condition = from(schemas.containerRecordSchemaType())
				.where(schemas.containerAdministrativeUnit()).isIn(units)
				.andWhere(schemas.containerDecommissioningType()).isEqualTo(params.type)
				.andWhere(schemas.containerStorageSpace()).is(params.withStorage ? isNotNull() : isNull());

		return searchServices.getResultsCount(condition);
	}

	public long getVisibleSubAdministrativeUnitCount(String administrativeUnitId) {

		LogicalSearchCondition condition = from(schemas.administrativeUnitSchemaType())
				.where(schemas.administrativeUnitParent()).is(administrativeUnitId)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();
		return searchServices.getResultsCount(condition);
	}

	private LogicalSearchCondition fromFolderWhereFilingSpaceAndAdministrativeUnitAre(String adminUnitId) {
		MetadataSchema folderSchema = schemas.defaultFolderSchema();
		Metadata administrativeUnit = folderSchema.getMetadata(Folder.ADMINISTRATIVE_UNIT);

		return from(schemas.folderSchemaType()).where(administrativeUnit).isEqualTo(adminUnitId);
	}

	public static class ContainerSearchParameters {

		String userId;
		String adminUnitId;
		DecommissioningType type;
		boolean withStorage;

		public String getUserId() {
			return userId;
		}

		public ContainerSearchParameters setUserId(String userId) {
			this.userId = userId;
			return this;
		}

		public String getAdminUnitId() {
			return adminUnitId;
		}

		public ContainerSearchParameters setAdminUnitId(String adminUnitId) {
			this.adminUnitId = adminUnitId;
			return this;
		}

		public DecommissioningType getType() {
			return type;
		}

		public ContainerSearchParameters setType(DecommissioningType type) {
			this.type = type;
			return this;
		}

		public boolean isWithStorage() {
			return withStorage;
		}

		public ContainerSearchParameters setWithStorage(boolean withStorage) {
			this.withStorage = withStorage;
			return this;
		}

		public void validate() {
			if (userId == null) {
				throw new ImpossibleRuntimeException("userId required");
			}
			if (type == null) {
				throw new ImpossibleRuntimeException("type required");
			}
			if (adminUnitId == null) {
				throw new ImpossibleRuntimeException("adminUnitId required");
			}

		}
	}
}
