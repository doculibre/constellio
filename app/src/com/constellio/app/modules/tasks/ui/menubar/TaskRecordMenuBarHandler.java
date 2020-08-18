package com.constellio.app.modules.tasks.ui.menubar;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.menuBar.AbstractRecordMenuBarHandler;
import com.constellio.app.ui.framework.components.menuBar.RecordVOMenuBar;
import com.vaadin.ui.MenuBar;

import java.io.IOException;
import java.util.Collections;

public class TaskRecordMenuBarHandler extends AbstractRecordMenuBarHandler {
	public TaskRecordMenuBarHandler(AppLayerFactory appLayerFactory) {
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
		if (Task.SCHEMA_TYPE.equals(schemaTypeCode)) {
			menuBarForSchemaTypeCode = true;
		} else {
			menuBarForSchemaTypeCode = false;
		}
		return menuBarForSchemaTypeCode;
	}

	@Override
	public MenuBar get(RecordVO recordVO) {
		if (recordVO == null || recordVO.getSchema() == null) {
			return null;
		} else {
			String schemaTypeCode = recordVO.getSchema().getTypeCode();
			if (Task.SCHEMA_TYPE.equals(schemaTypeCode)) {
				return new RecordVOMenuBar(recordVO, Collections.emptyList(), appLayerFactory);
			} else {
				return null;
			}
		}
	}
}
