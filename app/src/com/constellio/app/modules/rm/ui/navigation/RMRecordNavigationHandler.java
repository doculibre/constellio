package com.constellio.app.modules.rm.ui.navigation;

import java.io.IOException;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.navigation.AbstractRecordNavigationHandler;

public class RMRecordNavigationHandler extends AbstractRecordNavigationHandler {

	public RMRecordNavigationHandler(ConstellioFactories constellioFactories) {
		super(constellioFactories);
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
	}

	@Override
	public boolean isViewForSchemaTypeCode(String schemaTypeCode) {
		boolean viewForSchemaTypeCode;
		if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
			viewForSchemaTypeCode = true;
		} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
			viewForSchemaTypeCode = true;
		} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
			viewForSchemaTypeCode = true;
		} else
			viewForSchemaTypeCode = RetentionRule.SCHEMA_TYPE.equals(schemaTypeCode);
		return viewForSchemaTypeCode;
	}

	@Override
	protected void navigateToView(String recordId, String schemaTypeCode) {
		if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
			ConstellioUI.getCurrent().navigate().to(RMViews.class).displayFolder(recordId);
		} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
			ConstellioUI.getCurrent().navigate().to(RMViews.class).displayDocument(recordId);
		} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
			ConstellioUI.getCurrent().navigate().to(RMViews.class).displayContainer(recordId);
		} else if (RetentionRule.SCHEMA_TYPE.equals(schemaTypeCode)) {
			ConstellioUI.getCurrent().navigate().to(RMViews.class).displayRetentionRule(recordId);
		} else {
			throw new UnsupportedOperationException("No navigation for schema type code " + schemaTypeCode);
		}
	}
}
