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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.model.enums.RetentionType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
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

	public LogicalSearchCondition bySearchType(SearchType type, String filingSpaceId, String adminUnitId) {
		switch (type) {
		case fixedPeriod:
			return withoutClosingDateAndWithFixedPeriod(filingSpaceId, adminUnitId);
		case code888:
			return withoutClosingDateAndWith888Period(filingSpaceId, adminUnitId);
		case code999:
			return withoutClosingDateAndWith999Period(filingSpaceId, adminUnitId);
		case transfer:
			return activeToTransferToSemiActive(filingSpaceId, adminUnitId);
		case activeToDeposit:
			return activeToDeposit(filingSpaceId, adminUnitId);
		case activeToDestroy:
			return activeToDestroy(filingSpaceId, adminUnitId);
		case semiActiveToDeposit:
			return semiActiveToDeposit(filingSpaceId, adminUnitId);
		case semiActiveToDestroy:
			return semiActiveToDestroy(filingSpaceId, adminUnitId);
		default:
			throw new RuntimeException("Unknown search type: " + type);
		}
	}

	public LogicalSearchCondition withoutClosingDateAndWithFixedPeriod(String filingSpaceId, String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(filingSpaceId, adminUnitId)
				.andWhere(schemas.folderActiveRetentionType()).isEqualTo(RetentionType.FIXED)
				.andWhere(schemas.folderCloseDate()).isNull()
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(ACTIVE);
	}

	public LogicalSearchCondition withoutClosingDateAndWith888Period(String filingSpaceId, String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(filingSpaceId, adminUnitId)
				.andWhere(schemas.folderActiveRetentionType()).isEqualTo(RetentionType.OPEN)
				.andWhere(schemas.folderCloseDate()).isNull()
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(ACTIVE);
	}

	public LogicalSearchCondition withoutClosingDateAndWith999Period(String filingSpaceId, String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(filingSpaceId, adminUnitId)
				.andWhere(schemas.folderActiveRetentionType()).isEqualTo(RetentionType.UNTIL_REPLACED)
				.andWhere(schemas.folderCloseDate()).isNull()
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(ACTIVE);
	}

	public LogicalSearchCondition activeToTransferToSemiActive(String filingSpaceId, String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(filingSpaceId, adminUnitId)
				.andWhere(schemas.folderPlanifiedTransferDate()).isLessOrEqualThan(getLocalDate())
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(ACTIVE);
	}

	public LogicalSearchCondition activeToDestroy(String filingSpaceId, String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(filingSpaceId, adminUnitId)
				.andWhere(schemas.folderPlanifiedDestructionDate()).isLessOrEqualThan(getLocalDate())
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(ACTIVE);
	}

	public LogicalSearchCondition activeToDeposit(String filingSpaceId, String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(filingSpaceId, adminUnitId)
				.andWhere(schemas.folderPlanifiedDepositDate()).isLessOrEqualThan(getLocalDate())
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(ACTIVE);
	}

	public LogicalSearchCondition semiActiveToDestroy(String filingSpaceId, String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(filingSpaceId, adminUnitId)
				.andWhere(schemas.folderPlanifiedDestructionDate()).isLessOrEqualThan(getLocalDate())
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(SEMI_ACTIVE);
	}

	public LogicalSearchCondition semiActiveToDeposit(String filingSpaceId, String adminUnitId) {
		return fromFolderWhereFilingSpaceAndAdministrativeUnitAre(filingSpaceId, adminUnitId)
				.andWhere(schemas.folderPlanifiedDepositDate()).isLessOrEqualThan(getLocalDate())
				.andWhere(schemas.folderArchivisticStatus()).isEqualTo(SEMI_ACTIVE);
	}

	public LogicalSearchCondition getVisibleContainersCondition(ContainerSearchParameters params) {
		params.validate();
		if (params.filingSpaceId == null) {
			throw new ImpossibleRuntimeException("filingSpaceId required");
		}

		return from(schemas.containerRecordSchemaType())
				.where(schemas.containerAdministrativeUnit()).isEqualTo(params.adminUnitId)
				.andWhere(schemas.containerFilingSpace()).isEqualTo(params.filingSpaceId)
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

		if (params.filingSpaceId != null) {
			condition = condition.andWhere(schemas.containerFilingSpace()).isEqualTo(params.filingSpaceId);
		}

		return searchServices.getResultsCount(condition);
	}

	public int getVisibleFilingSpacesCount(String administrativeUnitId) {
		Set<String> filingSpaces = new HashSet<>();
		for (AdministrativeUnit unit : decommissioningService.getAllAdminUnitHierarchyOf(administrativeUnitId)) {
			filingSpaces.addAll(unit.getFilingSpaces());
		}

		return filingSpaces.size();
	}

	private LogicalSearchCondition fromFolderWhereFilingSpaceAndAdministrativeUnitAre(String filingSpaceId, String adminUnitId) {
		MetadataSchema folderSchema = schemas.defaultFolderSchema();
		Metadata filingSpace = folderSchema.getMetadata(Folder.FILING_SPACE);
		Metadata administrativeUnit = folderSchema.getMetadata(Folder.ADMINISTRATIVE_UNIT);

		return from(schemas.folderSchemaType())
				.where(filingSpace).isEqualTo(filingSpaceId).andWhere(administrativeUnit).isEqualTo(adminUnitId);
	}

	public static class ContainerSearchParameters {

		String userId;
		String filingSpaceId;
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

		public String getFilingSpaceId() {
			return filingSpaceId;
		}

		public ContainerSearchParameters setFilingSpaceId(String filingSpaceId) {
			this.filingSpaceId = filingSpaceId;
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
