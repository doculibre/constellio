package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;

public class TaskLinkedFoldersFieldImpl extends LookupFolderField implements TaskLinkedFoldersField {

	@Override
	public String getFieldValue() {
		return (String) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((String) value);
	}
}
