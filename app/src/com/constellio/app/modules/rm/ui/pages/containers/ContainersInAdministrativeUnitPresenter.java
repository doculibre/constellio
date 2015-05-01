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
package com.constellio.app.modules.rm.ui.pages.containers;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ContainersInAdministrativeUnitPresenter extends BasePresenter<ContainersInAdministrativeUnitView> {

	public static final String DEPOSIT_PREFIX = "deposit";
	public static final String TRANSFER_PREFIX = "transfer";
	public static final String WITH_STORAGE_SPACE_SUFFIX = "WithStorageSpace";

	String adminUnitId;
	String tabName;

	public ContainersInAdministrativeUnitPresenter(ContainersInAdministrativeUnitView view) {
		super(view);
	}

	public RecordVODataProvider getChildrenAdminUnitsDataProvider() {
		final MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(schema(AdministrativeUnit.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());
		RecordVODataProvider dataProvider = new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				DecommissioningService service = new DecommissioningService(
						view.getCollection(), modelLayerFactory);
				List<String> visibleAdminUnitsId = service.getAllAdminUnitIdsHierarchyOf(adminUnitId);
				MetadataSchema schema = schema(schemaVO.getCode());
				LogicalSearchCondition condition;
				if (!visibleAdminUnitsId.isEmpty()) {
					condition = LogicalSearchQueryOperators.from(schema)
							.where(schema.getMetadata(AdministrativeUnit.PARENT)).isIn(visibleAdminUnitsId);
				} else {
					condition = LogicalSearchQueryOperators.from(schema).where(Schemas.TOKENS).isContaining(Arrays.asList("A38"));
				}
				return new LogicalSearchQuery(condition);
			}
		};
		return dataProvider;
	}

	public RecordVODataProvider getFilingSpacesDataProvider() {
		final MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(schema(FilingSpace.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());
		RecordVODataProvider dataProvider = new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				AdministrativeUnit adminUnit = new AdministrativeUnit(
						modelLayerFactory.newRecordServices().getDocumentById(adminUnitId),
						modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(view.getCollection()));
				MetadataSchema schema = schema(schemaVO.getCode());
				LogicalSearchCondition condition;
				if (!adminUnit.getFilingSpaces().isEmpty()) {
					condition = LogicalSearchQueryOperators.from(schema).where(Schemas.IDENTIFIER)
							.isIn(adminUnit.getFilingSpaces());
				} else {
					condition = LogicalSearchQueryOperators.from(schema).where(Schemas.TOKENS).isContaining(Arrays.asList("A38"));
				}
				return new LogicalSearchQuery(condition).filteredWithUser(modelLayerFactory.newUserServices()
						.getUserInCollection(view.getSessionContext().getCurrentUser().getUsername(), view.getCollection()));
			}
		};
		return dataProvider;
	}

	public RecordVO getAdministrativeUnit() {
		return new RecordToVOBuilder()
				.build(recordServices().getDocumentById(adminUnitId), VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public void forParams(String params) {
		String[] splitParams = params.split("/");
		tabName = splitParams[0];
		adminUnitId = splitParams[1];
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_CONTAINERS).globally();
	}

	public void displayAdminUnitButtonClicked(String tabName, RecordVO adminUnit) {
		view.navigateTo().displayAdminUnitWithContainers(tabName, adminUnit.getId());
	}

	public void displayFilingSpaceButtonClicked(String tabName, RecordVO filingSpace) {
		view.navigateTo().displayFilingSpaceWithContainers(tabName, adminUnitId, filingSpace.getId());
	}
}
