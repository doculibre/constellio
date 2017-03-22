package com.constellio.app.ui.framework.components.contextmenu;

import java.io.IOException;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;

public abstract class AbstractRecordContextMenuHandler implements RecordContextMenuHandler {
	
	protected transient ConstellioFactories constellioFactories;
	
	public AbstractRecordContextMenuHandler(ConstellioFactories constellioFactories) {
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
	public boolean isContextMenuForRecordId(String recordId) {
		RecordServices recordServices = constellioFactories.getModelLayerFactory().newRecordServices();
		Record record = recordServices.getDocumentById(recordId);
		String schemaCode = record.getSchemaCode();
		return isContextMenuForSchemaCode(schemaCode);
	}

	@Override
	public boolean isContextMenuForSchemaCode(String schemaCode) {
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		return isContextMenuForSchemaTypeCode(schemaTypeCode);
	}

	@Override
	public boolean isContextMenu(RecordVO recordVO) {
		String schemaCode = recordVO.getSchema().getCode();
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		return isContextMenuForSchemaTypeCode(schemaTypeCode);
	}
	
	protected String getSchemaTypeCodeForSchemaCode(String schemaCode) {
		return SchemaUtils.getSchemaTypeCode(schemaCode);
	}

	@Override
	public RecordContextMenu getForSchemaCode(String schemaCode) {
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		return getForSchemaTypeCode(schemaTypeCode);
	}

	@Override
	public RecordContextMenu getForRecordId(String recordId) {
		RecordServices recordServices = constellioFactories.getModelLayerFactory().newRecordServices();
		Record record = recordServices.getDocumentById(recordId);
		String schemaCode = record.getSchemaCode();
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		return getForSchemaTypeCode(schemaTypeCode);
	}

	@Override
	public RecordContextMenu get(RecordVO recordVO) {
		String schemaCode = recordVO.getSchema().getCode();
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		return getForSchemaCode(schemaTypeCode);
	}

}
