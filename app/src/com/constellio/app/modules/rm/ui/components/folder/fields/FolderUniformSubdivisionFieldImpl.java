package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

public class FolderUniformSubdivisionFieldImpl extends LookupRecordField implements FolderUniformSubdivisionField {

	public FolderUniformSubdivisionFieldImpl() {
		super(UniformSubdivision.SCHEMA_TYPE, true);
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
