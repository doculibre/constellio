package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.vaadin.data.util.converter.Converter;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class TaskAssignationListRecordLookupField extends ListAddRemoveRecordLookupField {
	public TaskAssignationListRecordLookupField(String schemaTypeCode) {
		super(schemaTypeCode);
	}

	public TaskAssignationListRecordLookupField(String schemaTypeCode, String schemaCode) {
		super(schemaTypeCode, schemaCode);
	}

	@Override
	public void setValue(List<String> newFieldValue) throws ReadOnlyException, Converter.ConversionException {
		super.setValue(newFieldValue, false, true);
	}

	@Override
	protected void removeValue(Object value) {
		if (value != null) {
			super.removeValue(value);

			fireEvent(new ValueChangeEvent(this));
		}
	}

	@Override
	protected void removeValue(String value) {
		if (value != null) {
			super.removeValue(value);

			fireEvent(new ValueChangeEvent(this));
		}
	}

	@Override
	protected String getReadOnlyMessage() {
		return $("TaskAssignationListRecordLookupField.readOnlyMessage");
	}
}
