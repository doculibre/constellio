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

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

public class ListSchemaRecordsPresenter extends SingleSchemaBasePresenter<ListSchemaRecordsView> {

	public ListSchemaRecordsPresenter(ListSchemaRecordsView view) {
		super(view);
	}

	public void forSchema(String parameters) {
		setSchemaCode(parameters);
	}

	public RecordVODataProvider getDataProvider() {
		String schemaCode = getSchemaCode();
		List<String> metadataCodes = new ArrayList<String>();
		metadataCodes.add(schemaCode + "_id");
		//		metadataCodes.add(schemaCode + "_code");
		metadataCodes.add(schemaCode + "_title");
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(schema(), VIEW_MODE.TABLE, metadataCodes);
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		RecordVODataProvider dataProvider = new RecordVODataProvider(schemaVO, voBuilder, modelLayerFactory) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(LogicalSearchQueryOperators.from(schema()).returnAll());
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
		Record record = getRecord(recordVO.getId());
		delete(record);
		view.refreshTable();
	}

	@Override
	protected boolean hasPageAccess(String params, final User user) {
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(params);
		return new SchemaRecordsPresentersServices(appLayerFactory).canManageSchemaType(schemaTypeCode, user);

	}

}
