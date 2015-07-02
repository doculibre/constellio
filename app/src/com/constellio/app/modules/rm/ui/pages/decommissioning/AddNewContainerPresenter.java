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

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Arrays;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;

public class AddNewContainerPresenter extends SingleSchemaBasePresenter<AddNewContainerView> {
	private transient RMSchemasRecordsServices rmRecordsServices;
	String recordId;

	public AddNewContainerPresenter(AddNewContainerView view) {
		super(view, ContainerRecord.DEFAULT_SCHEMA);
	}

	public AddNewContainerPresenter forRecordId(String recordId) {
		this.recordId = recordId;
		return this;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).globally();
	}

	public RecordVO getContainer() {
		/*Record record = newRecord();
		ContainerRecord container = rmRecordsServices().wrapContainerRecord(record);
		container.setCapacity();*/
		return new RecordToVOBuilder().build(newRecord(), VIEW_MODE.FORM);
	}

	public void saveButtonClicked(RecordVO recordVO) {
		DecommissioningList decommissioningList = rmRecordsServices().getDecommissioningList(recordId);

		ContainerRecord container = rmRecordsServices().wrapContainerRecord(toRecord(recordVO));
		container.setAdministrativeUnit(decommissioningList.getAdministrativeUnit());
		container.setFilingSpace(decommissioningList.getFilingSpace());

		decommissioningList.addContainerDetailsFrom(Arrays.asList(container));

		Transaction transaction = new Transaction().setUser(getCurrentUser());
		transaction.addAll(container, decommissioningList);
		try {
			recordServices().execute(transaction);
			view.navigateTo().displayDecommissioningList(recordId);
		} catch (Exception e) {
			view.showErrorMessage($("AddNewContainerView.failedToSave"));
		}
	}

	public void cancelButtonClicked(RecordVO recordVO) {
		view.navigateTo().displayDecommissioningList(recordId);
	}

	private RMSchemasRecordsServices rmRecordsServices() {
		if (rmRecordsServices == null) {
			rmRecordsServices = new RMSchemasRecordsServices(view.getCollection(), modelLayerFactory);
		}
		return rmRecordsServices;
	}
}
