package com.constellio.app.ui.menuBar;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.menuBar.AbstractRecordMenuBarHandler;
import com.constellio.app.ui.framework.components.menuBar.RecordVOMenuBar;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.MenuBar;

import java.io.IOException;
import java.util.Collections;

public class CoreRecordMenuBarHandler extends AbstractRecordMenuBarHandler {

	public CoreRecordMenuBarHandler(AppLayerFactory appLayerFactory) {
		super(appLayerFactory);
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
	public boolean isMenuBarForSchemaTypeCode(String schemaTypeCode) {
		boolean menuBarForSchemaTypeCode;
		if (User.SCHEMA_TYPE.equals(schemaTypeCode) || Group.SCHEMA_TYPE.equals(schemaTypeCode)) {
			menuBarForSchemaTypeCode = true;
		} else {
			menuBarForSchemaTypeCode = false;
		}
		return menuBarForSchemaTypeCode;
	}

	@Override
	public MenuBar get(RecordVO recordVO) {
		String schemaTypeCode = recordVO == null ? null : recordVO.getSchema().getTypeCode();
		if (recordVO != null && isSchemaTypeSupported(schemaTypeCode)) {
			return new RecordVOMenuBar(recordVO, Collections.emptyList(), appLayerFactory);
		} else {
			return null;
		}
	}

	private boolean isSchemaTypeSupported(String schemaTypeCode) {
		return User.SCHEMA_TYPE.equals(schemaTypeCode) || Group.SCHEMA_TYPE.equals(schemaTypeCode);
	}

}

