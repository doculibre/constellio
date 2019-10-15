package com.constellio.app.ui.framework.components.menuBar;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;

import java.io.IOException;

public abstract class AbstractRecordMenuBarHandler implements RecordMenuBarHandler {

	protected transient AppLayerFactory appLayerFactory;

	public AbstractRecordMenuBarHandler(AppLayerFactory appLayerFactory) {
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
	public boolean isMenuBarForRecordId(String recordId) {
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
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
