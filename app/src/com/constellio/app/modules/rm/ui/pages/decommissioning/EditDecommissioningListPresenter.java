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
package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class EditDecommissioningListPresenter extends SingleSchemaBasePresenter<EditDecommissioningListView> {
	String recordId;

	public EditDecommissioningListPresenter(EditDecommissioningListView view) {
		super(view, DecommissioningList.DEFAULT_SCHEMA);
	}

	public EditDecommissioningListPresenter forRecordId(String recordId) {
		this.recordId = recordId;
		return this;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).globally();
	}

	public RecordVO getDecommissioningList() {
		return presenterService().getRecordVO(recordId, VIEW_MODE.FORM);
	}

	public void saveButtonClicked(RecordVO recordVO) {
		try {
			Record record = toRecord(recordVO);
			addOrUpdate(record);
			view.navigateTo().displayDecommissioningList(recordId);
		} catch (Exception e) {
			view.showErrorMessage("Failed to save");
		}
	}

	public void cancelButtonClicked(RecordVO recordVO) {
		view.navigateTo().displayDecommissioningList(recordVO.getId());
	}
}
