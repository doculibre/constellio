package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

@SuppressWarnings("unchecked")
public class ListAddRemoveRecordLookupField extends ListAddRemoveField<String, LookupRecordField> {
	
	private String schemaTypeCode;
	private String schemaCode;
	private boolean ignoreLinkability;

	public ListAddRemoveRecordLookupField(String schemaTypeCode) {
		this(schemaTypeCode, null);
	}

	public ListAddRemoveRecordLookupField(String schemaTypeCode, String schemaCode) {
		super();
		this.schemaTypeCode = schemaTypeCode;
		this.schemaCode = schemaCode;
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
		LookupRecordField field = new LookupRecordField(schemaTypeCode, schemaCode);
		field.setIgnoreLinkability(ignoreLinkability);
		return field;
	}
}
