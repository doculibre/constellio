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
package com.constellio.app.modules.rm.ui.pages.administrativeUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

@SuppressWarnings("serial")
public class ListAdministrativeUnitsPresenter extends SingleSchemaBasePresenter<ListAdministrativeUnitsView> {

	// FIXME Hard-coded values
	private static final String PREFIX = "administrativeUnit_default_";
	private static final String PROPERTY_ID = PREFIX + "id";
	private static final String PROPERTY_CODE = PREFIX + AdministrativeUnit.CODE;
	private static final String PROPERTY_TITLE = PREFIX + "title";

	public ListAdministrativeUnitsPresenter(ListAdministrativeUnitsView view) {
		super(view, AdministrativeUnit.DEFAULT_SCHEMA);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {		
		stream.defaultReadObject();
		init();
	}

	private void init() {
	}

	public RecordVODataProvider getDataProvider() {
		List<String> metadataCodes = new ArrayList<String>();
		metadataCodes.add(PROPERTY_ID);
		metadataCodes.add(PROPERTY_CODE);
		metadataCodes.add(PROPERTY_TITLE);

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
		view.navigateTo().displayAdministrativeUnit(recordVO.getId());
	}

	public void editButtonClicked(RecordVO recordVO) {
		view.navigateTo().editAdministrativeUnit(recordVO.getId());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		Record record = getRecord(recordVO.getId());
		delete(record);
		view.refreshTable();
	}

	public void addLinkClicked() {
		view.navigateTo().addAdministrativeUnit();
	}

	public RecordVO getRecordVO(String id) {
		Record record = recordServices().getDocumentById(id);
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		return voBuilder.build(record, VIEW_MODE.TABLE);
	}

}
