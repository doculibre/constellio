package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

public class FolderTypeFieldLookupImpl extends LookupRecordField implements FolderTypeField {

	public FolderTypeFieldLookupImpl() {
		super(FolderType.SCHEMA_TYPE);
	}

	@Override
	public String getFieldValue() {
		return (String) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((String) value);
	}

}
