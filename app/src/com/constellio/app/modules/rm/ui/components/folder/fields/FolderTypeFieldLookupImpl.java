package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

public class FolderTypeFieldLookupImpl extends LookupRecordField implements FolderTypeField {

	public FolderTypeFieldLookupImpl(String parent) {
		super(FolderType.SCHEMA_TYPE, null, false,
				new FolderTypeTextInputDataProvider(getInstance(), getCurrentSessionContext(), parent));
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
