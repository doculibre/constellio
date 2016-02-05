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
			throw new RuntimeException(e);
		}
		view.navigateTo().listConnectorInstances();
	}

	@Override
	public String getTitle() {
		return $("EditConnectorInstanceView.viewTitle");
	}
}
