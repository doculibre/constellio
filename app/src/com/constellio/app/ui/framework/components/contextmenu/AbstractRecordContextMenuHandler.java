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
		return new SchemaUtils().getSchemaTypeCode(schemaCode);
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
