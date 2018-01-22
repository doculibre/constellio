package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;
import com.vaadin.data.Item;

import java.util.List;

public class TaskTypeFieldComboBoxImpl extends RecordComboBox implements TaskTypeField {

	List<String> unavailablesTaskTypes;

	public TaskTypeFieldComboBoxImpl(List<String> unavailablesTaskTypes) {
		super(TaskType.DEFAULT_SCHEMA);
		setImmediate(true);
		this.unavailablesTaskTypes = unavailablesTaskTypes;
		removeUnavailablesTaskTypes();
	}

	public void removeUnavailablesTaskTypes() {
		if(unavailablesTaskTypes != null) {
			for(String type: unavailablesTaskTypes) {
				removeItem(type);
			}
		}
	}

	@Override
	public String getFieldValue() {
		return (String) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((String) value);
	}

	@Override
	public Item addItem(Object itemId) throws UnsupportedOperationException {
		if(unavailablesTaskTypes == null || !unavailablesTaskTypes.contains(itemId)) {
			return super.addItem(itemId);
		}
		return null;
	}
}
