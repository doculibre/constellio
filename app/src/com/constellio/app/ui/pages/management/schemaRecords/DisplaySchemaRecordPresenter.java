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

import java.io.IOException;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;

@SuppressWarnings("serial")
public class DisplaySchemaRecordPresenter extends SingleSchemaBasePresenter<DisplaySchemaRecordView> {

	public DisplaySchemaRecordPresenter(DisplaySchemaRecordView view) {
		super(view);
		init();
	}

	public void forSchema(String schemaCode) {
		setSchemaCode(schemaCode);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
	}

	public RecordVO getRecordVO(String id) {
		return presenterService().getRecordVO(id, VIEW_MODE.DISPLAY);
	}

	public void backButtonClicked() {
		String schemaCode = getSchemaCode();
		view.navigateTo().listSchemaRecords(schemaCode);
	}

	public void editButtonClicked(RecordVO recordVO) {
		String schemaCode = getSchemaCode();
		view.navigateTo().editSchemaRecord(schemaCode, recordVO.getId());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		String schemaCode = getSchemaCode();
		view.navigateTo().listSchemaRecords(schemaCode);
	}
}
