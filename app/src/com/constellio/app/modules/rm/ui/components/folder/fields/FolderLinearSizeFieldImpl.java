package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.ui.framework.components.fields.number.BaseDoubleField;

public class FolderLinearSizeFieldImpl extends BaseDoubleField implements FolderLinearSizeField {
	@Override
	public String getFieldValue() {
		return getConvertedValue().toString();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((String) value);
	}
}
