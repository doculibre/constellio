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
package com.constellio.app.ui.pages.base;

import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordServicesRuntimeException;

public abstract class SingleSchemaBasePresenter<T extends BaseView> extends BasePresenter<T> {

	private SchemaPresenterUtils schemaPresenterUtils;

	public SingleSchemaBasePresenter(T view) {
		this(view, "");
	}

	public SingleSchemaBasePresenter(T view, String schemaCode) {
		this(view, schemaCode, view.getConstellioFactories(), view.getSessionContext());
	}

	public SingleSchemaBasePresenter(T view, String schemaCode, ConstellioFactories constellioFactories,
			SessionContext sessionContext) {
		super(view, constellioFactories, sessionContext);
		this.schemaPresenterUtils = new SchemaPresenterUtils(schemaCode, constellioFactories, sessionContext);
	}

	public final String getSchemaCode() {
		return schemaPresenterUtils.schemaCode;
	}

	public final void setSchemaCode(String schemaCode) {
		this.schemaPresenterUtils.setSchemaCode(schemaCode);
	}

	protected Record newRecord() {
		return schemaPresenterUtils.newRecord();
	}

	protected Record getRecord(String id) {
		return schemaPresenterUtils.getRecord(id);
	}

	public final Metadata getMetadata(String code) {
		return schemaPresenterUtils.getMetadata(code);
	}

	protected List<BatchProcess> addOrUpdate(Record record) {
		return schemaPresenterUtils.addOrUpdate(record);
	}

	protected final void delete(Record record) {
		delete(record, null, true);
	}

	protected final void delete(Record record, String reason) {
		delete(record, reason, true);
	}

	protected final void delete(Record record, boolean physically) {
		delete(record, null, physically);
	}

	protected final void delete(Record record, String reason, boolean physically) {
		try {
			schemaPresenterUtils.delete(record, reason, physically);
		} catch (RecordServicesRuntimeException exception) {
			view.showErrorMessage(MessageUtils.toMessage(exception));
		}
	}

	protected Record toRecord(RecordVO recordVO) {
		return schemaPresenterUtils.toRecord(recordVO);
	}

	protected MetadataSchema schema() {
		return schemaPresenterUtils.schema();
	}
}
