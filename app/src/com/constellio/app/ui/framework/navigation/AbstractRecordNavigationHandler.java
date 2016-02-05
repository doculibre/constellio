package com.constellio.app.ui.framework.navigation;

import java.io.IOException;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;

public abstract class AbstractRecordNavigationHandler implements RecordNavigationHandler {
	
	protected transient ConstellioFactories constellioFactories;
	
	public AbstractRecordNavigationHandler(ConstellioFactories constellioFactories) {
		this.constellioFactories = constellioFactories;
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		if (constellioFactories == null) {
			constellioFactories = ConstellioFactories.getInstance();
		}
	}
	
	@Override
	public boolean isViewForRecordId(String recordId) {
		String schemaCode = getSchemaCodeForRecordId(recordId);
		return isViewForSchemaCode(schemaCode);
	}

	@Override
	public boolean isViewForSchemaCode(String schemaCode) {
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		return isViewForSchemaTypeCode(schemaTypeCode);
	}

	@Override
	public boolean isView(RecordVO recordVO) {
		String schemaCode = recordVO.getSchema().getCode();
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		return isViewForSchemaTypeCode(schemaTypeCode);
	}
	
	protected String getSchemaCodeForRecordId(String recordId) {
		RecordServices recordServices = constellioFactories.getModelLayerFactory().newRecordServices();
		Record record = recordServices.getDocumentById(recordId);
		return record.getSchemaCode();
	}
	
	protected String getSchemaTypeCodeForSchemaCode(String schemaCode) {
		return new SchemaUtils().getSchemaTypeCode(schemaCode);
	}

	@Override
	public void navigateToView(String recordId) {
		String schemaCode = getSchemaCodeForRecordId(recordId);
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		navigateToView(recordId, schemaTypeCode);
	}

	@Override
	public void navigateToView(RecordVO recordVO) {
		String schemaCode = recordVO.getSchema().getCode();
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		navigateToView(recordVO.getId(), schemaTypeCode);
	}
	
	protected abstract void navigateToView(String recordId, String schemaTypeCode);

}
