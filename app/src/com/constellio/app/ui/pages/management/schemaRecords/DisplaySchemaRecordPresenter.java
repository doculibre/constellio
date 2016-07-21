package com.constellio.app.ui.pages.management.schemaRecords;

import java.io.IOException;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.management.sequence.SequenceServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

@SuppressWarnings("serial")
public class DisplaySchemaRecordPresenter extends SingleSchemaBasePresenter<DisplaySchemaRecordView> {
	
	private transient SequenceServices sequenceServices;

	public DisplaySchemaRecordPresenter(DisplaySchemaRecordView view) {
		super(view);
		initTransientObjects();
	}

	public void forSchema(String schemaCode) {
		setSchemaCode(schemaCode);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ConstellioFactories constellioFactories = view.getConstellioFactories();
		SessionContext sessionContext = view.getSessionContext();
		sequenceServices = new SequenceServices(constellioFactories, sessionContext);
	}

	public RecordVO getRecordVO(String id) {
		return presenterService().getRecordVO(id, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public void backButtonClicked() {
		String schemaCode = getSchemaCode();
		view.navigate().to().listSchemaRecords(schemaCode);
	}

	public void editButtonClicked(RecordVO recordVO) {
		String schemaCode = getSchemaCode();
		view.navigate().to().editSchemaRecord(schemaCode, recordVO.getId());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		String schemaCode = getSchemaCode();
		view.navigate().to().listSchemaRecords(schemaCode);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		Record restrictedRecord = recordServices().getDocumentById(params);
		return new SchemaRecordsPresentersServices(appLayerFactory).canViewSchemaTypeRecord(restrictedRecord, user);
	}

	public boolean isSequenceTable(RecordVO recordVO) {
		return !sequenceServices.getAvailableSequences(recordVO.getId()).isEmpty();
	}
	
}
