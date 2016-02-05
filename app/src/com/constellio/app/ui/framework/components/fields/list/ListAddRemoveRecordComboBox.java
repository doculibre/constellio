package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;

@SuppressWarnings("unchecked")
public class ListAddRemoveRecordComboBox extends ListAddRemoveField<String, RecordComboBox> {

	private String schemaCode;

	public ListAddRemoveRecordComboBox(String schemaCode) {
		super();
		this.schemaCode = schemaCode;
		setItemConverter(new RecordIdToCaptionConverter());
	}

	@Override
	protected RecordComboBox newAddEditField() {
		return new RecordComboBox(schemaCode);
	}

}
