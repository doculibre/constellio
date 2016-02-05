package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;

public abstract class AddEditConnectorInstancePresenter extends SingleSchemaBasePresenter<AddEditConnectorInstanceView> {

	protected String connectorTypeId;
	protected RecordToVOBuilder voBuilder = new RecordToVOBuilder();
	protected RecordVO recordVO;
	protected String currentSchemaCode;

	protected transient ESSchemasRecordsServices esSchemasRecordsServices;
	protected transient UserServices userServices;
	protected transient RecordServices recordServices;

	public AddEditConnectorInstancePresenter(AddEditConnectorInstanceView view) {
		super(view);
		init();
	}

	public void init() {
		esSchemasRecordsServices = new ESSchemasRecordsServices(collection, appLayerFactory);
		userServices = modelLayerFactory.newUserServices();
		recordServices = modelLayerFactory.newRecordServices();
	}

	public void forParams(String params) {
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_CONTAINERS).globally();
	}

	public void backButtonClicked() {
		view.navigateTo().listConnectorInstances();
	}

	public void cancelButtonClicked() {
		view.navigateTo().listConnectorInstances();
	}

	public abstract void saveButtonClicked(RecordVO recordVO);

	public void setConnectorTypeId(String connectorTypeId) {
		this.connectorTypeId = connectorTypeId;
	}

	public void setRecordVO(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	public void setCurrentSchemaCode(String currentSchemaCode) {
		this.currentSchemaCode = currentSchemaCode;
	}

	//	public String getConnectorTypeId() {
	//		return connectorTypeId;
	//	}
	//
	//	public RecordVO getRecordVO() {
	//		return recordVO;
	//	}
	//
	//	public String getCurrentSchemaCode() {
	//		return currentSchemaCode;
	//	}
	//
	//	public RecordToVOBuilder getVoBuilder() {
	//		return voBuilder;
	//	}

	public abstract String getTitle();
}
