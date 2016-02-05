package com.constellio.app.ui.pages.management.schemaRecords;

import java.io.IOException;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

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

	@Override
	protected boolean hasPageAccess(String params, User user) {
		Record restrictedRecord = recordServices().getDocumentById(params);
		return new SchemaRecordsPresentersServices(appLayerFactory).canViewSchemaTypeRecord(restrictedRecord, user);
	}
}
