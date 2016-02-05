package com.constellio.app.modules.rm.ui.components.folder.fields;

public class FolderParentFolderFieldImpl extends LookupFolderField implements FolderParentFolderField {

	public FolderParentFolderFieldImpl(String[] taxonomyCodes) {
		super();
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
