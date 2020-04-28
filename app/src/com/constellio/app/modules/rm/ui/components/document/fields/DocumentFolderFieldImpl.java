package com.constellio.app.modules.rm.ui.components.document.fields;

import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;

public class DocumentFolderFieldImpl extends LookupFolderField implements DocumentFolderField {

	public DocumentFolderFieldImpl(boolean writeAccess) {
		super(writeAccess);
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
