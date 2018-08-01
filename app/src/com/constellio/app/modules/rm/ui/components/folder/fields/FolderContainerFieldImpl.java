package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

public class FolderContainerFieldImpl extends LookupRecordField implements FolderContainerField {

	public FolderContainerFieldImpl() {
		super(ContainerRecord.SCHEMA_TYPE);
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
