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
package com.constellio.app.ui.pages.management.schemaRecords;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ListSchemaRecordsPresenter extends SingleSchemaBasePresenter<ListSchemaRecordsView> {

	public ListSchemaRecordsPresenter(ListSchemaRecordsView view) {
		super(view);
	}

	public void forSchema(String parameters) {
		setSchemaCode(parameters);
	}

	public RecordVODataProvider getDataProvider() {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(defaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		RecordVODataProvider dataProvider = new RecordVODataProvider(
				schemaVO, voBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(defaultSchema()).returnAll())
						.filteredByStatus(StatusFilter.ACTIVES).sortAsc(Schemas.TITLE);
			}
		};
		return dataProvider;
	}

	public void displayButtonClicked(RecordVO recordVO) {
		view.navigateTo().displaySchemaRecord(recordVO.getId());
	}

	public void editButtonClicked(RecordVO recordVO) {
		String schemaCode = getSchemaCode();
		view.navigateTo().editSchemaRecord(schemaCode, recordVO.getId());
	}

	public void addLinkClicked() {
		String schemaCode = getSchemaCode();
		view.navigateTo().addSchemaRecord(schemaCode);
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		if (isDeletable(recordVO)) {
			Record record = getRecord(recordVO.getId());
			try {
				delete(record);
			} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord error) {
			/*
				This catch happens to avoid presenting a message in the UI
				which wrongly tells the user that the deletion completely failed
				while it really succeeded, but only logically.
			 */
			}
			view.refreshTable();
		} else {
			view.showErrorMessage($("ListSchemaRecordsView.cannotDelete"));
		}

	}

	@Override
	protected boolean hasPageAccess(String params, final User user) {
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(params);
		return new SchemaRecordsPresentersServices(appLayerFactory).canManageSchemaType(schemaTypeCode, user);
	}

}
