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

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.ui.pages.containers.ContainersByAdministrativeUnitsView.ContainersViewTab;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ContainersByAdministrativeUnitsPresenter extends BasePresenter<ContainersByAdministrativeUnitsView> {

	public static final String TAB_TRANSFER_NO_STORAGE_SPACE = "transferNoStorageSpace";
	public static final String TAB_DEPOSIT_NO_STORAGE_SPACE = "depositNoStorageSpace";
	public static final String TAB_TRANSFER_WITH_STORAGE_SPACE = "transferWithStorageSpace";
	public static final String TAB_DEPOSIT_WITH_STORAGE_SPACE = "depositWithStorageSpace";

	private ContainersViewTab transferNoStorageSpace;
	private ContainersViewTab depositNoStorageSpace;
	private ContainersViewTab transferWithStorageSpace;
	private ContainersViewTab depositWithStorageSpace;

	private List<ContainersViewTab> tabs;

	public ContainersByAdministrativeUnitsPresenter(ContainersByAdministrativeUnitsView view) {
		super(view);
		init();
	}

	public void init() {
		transferNoStorageSpace = new ContainersViewTab(
				TAB_TRANSFER_NO_STORAGE_SPACE,
				getDataProvider());
		depositNoStorageSpace = new ContainersViewTab(
				TAB_DEPOSIT_NO_STORAGE_SPACE,
				getDataProvider());
		transferWithStorageSpace = new ContainersViewTab(
				TAB_TRANSFER_WITH_STORAGE_SPACE,
				getDataProvider());
		depositWithStorageSpace = new ContainersViewTab(
				TAB_DEPOSIT_WITH_STORAGE_SPACE,
				getDataProvider());

		tabs = Arrays.asList(transferNoStorageSpace, depositNoStorageSpace, transferWithStorageSpace, depositWithStorageSpace);
	}

	RecordVODataProvider getDataProvider() {

		List<String> metadataCodes = new ArrayList<String>();
		metadataCodes.add(AdministrativeUnit.DEFAULT_SCHEMA + "_id");
		metadataCodes.add(AdministrativeUnit.DEFAULT_SCHEMA + "_code");
		metadataCodes.add(AdministrativeUnit.DEFAULT_SCHEMA + "_title");

		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(schema(AdministrativeUnit.DEFAULT_SCHEMA), VIEW_MODE.TABLE, metadataCodes, view.getSessionContext());
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		RecordVODataProvider dataProvider = new RecordVODataProvider(schemaVO, voBuilder, modelLayerFactory,
				view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(schema(AdministrativeUnit.DEFAULT_SCHEMA))
						.where(schema(AdministrativeUnit.DEFAULT_SCHEMA).getMetadata(AdministrativeUnit.PARENT)).isNull());
			}
		};
		return dataProvider;
	}

	public void forParams(String params) {
		if (view.getTabs().isEmpty()) {
			view.setTabs(tabs);
		}

		String tabName = params;
		ContainersViewTab initialTab;
		if (TAB_TRANSFER_NO_STORAGE_SPACE.equals(tabName)) {
			initialTab = transferNoStorageSpace;
		} else if (TAB_DEPOSIT_NO_STORAGE_SPACE.equals(tabName)) {
			initialTab = depositNoStorageSpace;
		} else if (TAB_TRANSFER_WITH_STORAGE_SPACE.equals(tabName)) {
			initialTab = transferWithStorageSpace;
		} else if (TAB_DEPOSIT_WITH_STORAGE_SPACE.equals(tabName)) {
			initialTab = depositWithStorageSpace;
		} else {
			initialTab = null;
		}
		if (initialTab != null) {
			view.selectTab(initialTab);
		}
	}

	public void displayButtonClicked(String tabName, RecordVO entity) {
		view.navigateTo().displayAdminUnitWithContainers(tabName, entity.getId());
	}

	public void backButtonClicked() {
		view.navigateTo().archivesManagement();
	}

}
