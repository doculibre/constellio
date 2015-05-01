/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
