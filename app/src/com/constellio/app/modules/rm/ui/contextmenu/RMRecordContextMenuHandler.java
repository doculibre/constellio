package com.constellio.app.modules.rm.ui.contextmenu;

import java.io.IOException;

import com.constellio.app.modules.rm.ui.components.contextmenu.DocumentContextMenuImpl;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.contextmenu.AbstractRecordContextMenuHandler;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenu;
import com.constellio.model.entities.records.wrappers.Event;

public class RMRecordContextMenuHandler extends AbstractRecordContextMenuHandler {
	
	public RMRecordContextMenuHandler(ConstellioFactories constellioFactories) {
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
	public boolean isContextMenuForSchemaTypeCode(String schemaTypeCode) {
		boolean contextMenuForSchemaTypeCode;
		if (Document.SCHEMA_TYPE.equals(schemaTypeCode) || Event.SCHEMA_TYPE.equals(schemaTypeCode)) {
			contextMenuForSchemaTypeCode = true;
		} else {
			contextMenuForSchemaTypeCode = false;
		}
		return contextMenuForSchemaTypeCode;
	}

	@Override
	public RecordContextMenu getForSchemaTypeCode(String schemaTypeCode) {
		if (Document.SCHEMA_TYPE.equals(schemaTypeCode) || Event.SCHEMA_TYPE.equals(schemaTypeCode)) {
			return new DocumentContextMenuImpl();
		} else {
			throw new UnsupportedOperationException("No context menu for schema type code " + schemaTypeCode);
		}
	}

}
