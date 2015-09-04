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
package com.constellio.app.modules.es.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServicesException;

public class EditConnectorInstancePresenter extends AddEditConnectorInstancePresenter {

	public EditConnectorInstancePresenter(EditConnectorInstanceView view) {
		super(view);
	}

	@Override
	public void forParams(String params) {
		try {
			Record record = recordServices.getDocumentById(params);
			recordVO = voBuilder.build(record, VIEW_MODE.FORM, view.getSessionContext());
			setRecordVO(recordVO);
			setCurrentSchemaCode(recordVO.getSchema().getCode());
			setSchemaCode(currentSchemaCode);
			view.setRecordVO(recordVO);
		} catch (Exception e) {
			//TODO Thiago
			throw new RuntimeException("Invalid id");
		}
	}

	@Override
	public void saveButtonClicked(RecordVO recordVO) {
		setCurrentSchemaCode(recordVO.getSchema().getCode());
		Record record = toRecord(recordVO);
		try {
			recordServices.update(record);
		} catch (RecordServicesException e) {
			//TODO Thiago
			throw new RuntimeException(e);
		}
		view.navigateTo().listConnectorInstances();
	}

	@Override
	public String getTitle() {
		return $("EditConnectorInstanceView.viewTitle");
	}
}
