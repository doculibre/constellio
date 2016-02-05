package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.ui.framework.components.fields.record.RecordOptionGroup;

public class FolderTypeFieldOptionGroupImpl extends RecordOptionGroup implements FolderTypeField {

	public FolderTypeFieldOptionGroupImpl() {
		super(FolderType.DEFAULT_SCHEMA);
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
