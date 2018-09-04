package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.vaadin.data.util.converter.Converter;

import java.util.List;

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
	protected void addValue(String value) {
		if (value != null) {
			super.addValue(value);

			fireEvent(new ValueChangeEvent(this));
		}
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
}
