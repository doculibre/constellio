package com.constellio.app.ui.framework.components.menuBar;

import java.io.IOException;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;

public abstract class AbstractRecordMenuBarHandler implements RecordMenuBarHandler {
	
	protected transient ConstellioFactories constellioFactories;
	
	public AbstractRecordMenuBarHandler(ConstellioFactories constellioFactories) {
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
	public boolean isMenuBarForRecordId(String recordId) {
		RecordServices recordServices = constellioFactories.getModelLayerFactory().newRecordServices();
		Record record = recordServices.getDocumentById(recordId);
		String schemaCode = record.getSchemaCode();
		return isMenuBarForSchemaCode(schemaCode);
	}

	@Override
	public boolean isMenuBarForSchemaCode(String schemaCode) {
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		return isMenuBarForSchemaTypeCode(schemaTypeCode);
	}

	@Override
	public boolean isMenuBar(RecordVO recordVO) {
		String schemaCode = recordVO.getSchema().getCode();
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		return isMenuBarForSchemaTypeCode(schemaTypeCode);
	}
	
	protected String getSchemaTypeCodeForSchemaCode(String schemaCode) {
		return SchemaUtils.getSchemaTypeCode(schemaCode);
	}

}
