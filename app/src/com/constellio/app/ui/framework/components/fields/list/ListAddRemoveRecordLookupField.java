package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

@SuppressWarnings("unchecked")
public class ListAddRemoveRecordLookupField extends ListAddRemoveField<String, LookupRecordField> {
	private String schemaTypeCode;
	private boolean ignoreLinkability;

	public ListAddRemoveRecordLookupField(String schemaTypeCode) {
		super();
		this.schemaTypeCode = schemaTypeCode;
		setItemConverter(new RecordIdToCaptionConverter());
		ignoreLinkability = false;
	}

	public void setIgnoreLinkability(boolean ignoreLinkability) {
		this.ignoreLinkability = ignoreLinkability;
		if (addEditField != null) {
			addEditField.setIgnoreLinkability(ignoreLinkability);
		}
	}

	@Override
	protected LookupRecordField newAddEditField() {
		LookupRecordField field = new LookupRecordField(schemaTypeCode);
		field.setIgnoreLinkability(ignoreLinkability);
		return field;
	}
}
