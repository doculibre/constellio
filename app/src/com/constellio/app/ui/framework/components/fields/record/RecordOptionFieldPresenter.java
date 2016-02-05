package com.constellio.app.ui.framework.components.fields.record;

import java.io.Serializable;

import com.constellio.app.ui.framework.data.AllSchemaRecordVODataProvider;

public class RecordOptionFieldPresenter implements Serializable {
	
	private RecordOptionField recordField;

	public RecordOptionFieldPresenter(RecordOptionField recordField) {
		this.recordField = recordField;
	}
	
	public void forSchemaCode(String schemaCode) {
		String collection = recordField.getSessionContext().getCurrentCollection();
		recordField.setDataProvider(new AllSchemaRecordVODataProvider(schemaCode, collection));
	}

}
