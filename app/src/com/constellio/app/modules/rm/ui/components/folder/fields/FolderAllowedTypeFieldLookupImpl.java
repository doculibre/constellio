package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;

import java.util.List;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

public class FolderAllowedTypeFieldLookupImpl extends ListAddRemoveRecordLookupField implements FolderAllowedTypeField {

	public FolderAllowedTypeFieldLookupImpl() {
		super(FolderType.SCHEMA_TYPE, null, new RecordTextInputDataProvider(getInstance(),
				getCurrentSessionContext(), FolderType.SCHEMA_TYPE, false));
	}

	@Override
	public List<String> getFieldValue() {
		return (List<String>) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((List<String>) value);
	}

	@Override
	protected void fireValueChange(boolean repaintIsNotNeeded) {
	}
}
