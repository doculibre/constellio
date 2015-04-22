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
package com.constellio.app.ui.framework.navigation;

import java.io.IOException;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;

public abstract class AbstractRecordNavigationHandler implements RecordNavigationHandler {
	
	protected transient ConstellioFactories constellioFactories;
	
	public AbstractRecordNavigationHandler(ConstellioFactories constellioFactories) {
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
	public boolean isViewForRecordId(String recordId) {
		String schemaCode = getSchemaCodeForRecordId(recordId);
		return isViewForSchemaCode(schemaCode);
	}

	@Override
	public boolean isViewForSchemaCode(String schemaCode) {
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		return isViewForSchemaTypeCode(schemaTypeCode);
	}

	@Override
	public boolean isView(RecordVO recordVO) {
		String schemaCode = recordVO.getSchema().getCode();
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		return isViewForSchemaTypeCode(schemaTypeCode);
	}
	
	protected String getSchemaCodeForRecordId(String recordId) {
		RecordServices recordServices = constellioFactories.getModelLayerFactory().newRecordServices();
		Record record = recordServices.getDocumentById(recordId);
		return record.getSchemaCode();
	}
	
	protected String getSchemaTypeCodeForSchemaCode(String schemaCode) {
		return new SchemaUtils().getSchemaTypeCode(schemaCode);
	}

	@Override
	public void navigateToView(String recordId) {
		String schemaCode = getSchemaCodeForRecordId(recordId);
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		navigateToView(recordId, schemaTypeCode);
	}

	@Override
	public void navigateToView(RecordVO recordVO) {
		String schemaCode = recordVO.getSchema().getCode();
		String schemaTypeCode = getSchemaTypeCodeForSchemaCode(schemaCode);
		navigateToView(recordVO.getId(), schemaTypeCode);
	}
	
	protected abstract void navigateToView(String recordId, String schemaTypeCode);

}
