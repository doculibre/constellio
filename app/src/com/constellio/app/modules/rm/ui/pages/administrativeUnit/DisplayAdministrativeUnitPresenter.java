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

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;

@SuppressWarnings("serial")
public class DisplayAdministrativeUnitPresenter extends SingleSchemaBasePresenter<DisplayAdministrativeUnitView> {

	public DisplayAdministrativeUnitPresenter(DisplayAdministrativeUnitView view) {
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

	public RecordVO getRecordVO(String id) {
		Record record = recordServices().getDocumentById(id);
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		return voBuilder.build(record, VIEW_MODE.DISPLAY);
	}

	public void backButtonClicked(RecordVO recordVO) {
		view.navigateTo().listAdministrativeUnits();
	}

	public void editButtonClicked(RecordVO recordVO) {
		view.navigateTo().editAdministrativeUnit(recordVO.getId());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		view.navigateTo().listAdministrativeUnits();
	}

}
