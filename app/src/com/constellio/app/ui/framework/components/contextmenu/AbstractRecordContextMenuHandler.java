package com.constellio.app.ui.framework.components.contextmenu;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;

import java.io.IOException;

public abstract class AbstractRecordContextMenuHandler implements RecordContextMenuHandler {

	protected transient AppLayerFactory appLayerFactory;

	public AbstractRecordContextMenuHandler(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		if (appLayerFactory == null) {
			appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		}
	}

	@Override
	public boolean isContextMenuForRecordId(String recordId) {
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
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
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
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
