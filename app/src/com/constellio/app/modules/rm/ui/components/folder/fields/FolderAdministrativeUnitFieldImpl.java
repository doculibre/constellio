package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

public class FolderAdministrativeUnitFieldImpl extends LookupRecordField implements FolderAdministrativeUnitField {

	public FolderAdministrativeUnitFieldImpl() {
		super(AdministrativeUnit.SCHEMA_TYPE, true);
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
