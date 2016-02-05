package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

public class FolderCategoryFieldImpl extends LookupRecordField implements FolderCategoryField {

	public FolderCategoryFieldImpl() {
		super(Category.SCHEMA_TYPE);
		setOnlyLinkables(true);
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
