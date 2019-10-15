package com.constellio.app.modules.rm.ui.contextmenu;

import com.constellio.app.modules.rm.ui.components.contextmenu.DocumentContextMenuImpl;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.contextmenu.AbstractRecordContextMenuHandler;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenu;
import com.constellio.model.entities.records.wrappers.Event;

import java.io.IOException;

public class RMRecordContextMenuHandler extends AbstractRecordContextMenuHandler {

	public RMRecordContextMenuHandler(AppLayerFactory appLayerFactory) {
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
