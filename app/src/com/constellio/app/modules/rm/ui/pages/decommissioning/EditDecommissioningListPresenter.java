package com.constellio.app.modules.rm.ui.pages.decommissioning;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSecurityService;
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
		return true;
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return Arrays.asList(params);
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		DecommissioningList decommissioningList = rmRecordsServices().wrapDecommissioningList(restrictedRecord);
		return securityService().canModify(decommissioningList, user);
	}

	public RecordVO getDecommissioningList() {
		return presenterService().getRecordVO(recordId, VIEW_MODE.FORM, view.getSessionContext());
	}

	public void saveButtonClicked(RecordVO recordVO) {
		try {
			Record record = toRecord(recordVO);
			addOrUpdate(record);
			if (rmRecordsServices().wrapDecommissioningList(record).getDecommissioningListType().isFolderList()) {
				view.navigate().to(RMViews.class).displayDecommissioningList(recordId);
			} else {
				view.navigate().to(RMViews.class).displayDocumentDecommissioningList(recordId);
			}
		} catch (Exception e) {
			view.showErrorMessage("Failed to save");
		}
	}

	private DecommissioningSecurityService securityService() {
		return new DecommissioningSecurityService(collection, appLayerFactory);
	}

	private RMSchemasRecordsServices rmRecordsServices() {
		return new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public void cancelButtonClicked(RecordVO recordVO) {
		view.navigate().to(RMViews.class).displayDecommissioningList(recordVO.getId());
	}
}
