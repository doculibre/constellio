package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveEnumWithSmallCodeComboBox;
import com.vaadin.data.util.converter.Converter;

import java.io.Serializable;
import java.util.List;

public class TaskAssignationEnumField extends ListAddRemoveEnumWithSmallCodeComboBox {
	public TaskAssignationEnumField(Class enumWithSmallCodeClass) {
		super(enumWithSmallCodeClass);
	}

	@Override
	public void setValue(List newFieldValue) throws ReadOnlyException, Converter.ConversionException {
		super.setValue(newFieldValue, false, true);
	}

	@Override
	protected void addValue(Serializable value) {
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
	protected void removeValue(Serializable value) {
		if (value != null) {
			super.removeValue(value);

			fireEvent(new ValueChangeEvent(this));
		}
	}
}
