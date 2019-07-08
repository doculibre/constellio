package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.es.navigation.ESViews;
import com.constellio.app.modules.es.ui.pages.ConnectorUtil.ConnectionStatus;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServicesException;

import static com.constellio.app.ui.i18n.i18n.$;

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
		String schemaCode = recordVO.getSchema().getCode();
		setCurrentSchemaCode(schemaCode);
		Record record = toRecord(recordVO);

		ConnectorUtil.ConnectionStatusResult connectonStatusResult = ConnectorUtil
				.testAuthentication(schemaCode, record, esSchemasRecordsServices);

		if (connectonStatusResult.getConnectionStatus() != ConnectionStatus.Ok) {
			view.showErrorMessage(ConnectorUtil.getErrorMessage(connectonStatusResult));
			return;
		}

		try {
			recordServices.update(record);
			view.navigate().to(ESViews.class).displayConnectorInstance(record.getId());
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getTitle() {
		return $("EditConnectorInstanceView.viewTitle");
	}
}
