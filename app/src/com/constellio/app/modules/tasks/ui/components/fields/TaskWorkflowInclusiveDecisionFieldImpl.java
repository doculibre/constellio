package com.constellio.app.modules.tasks.ui.components.fields;

import com.vaadin.ui.ComboBox;

/**
 * Created by constellios on 2017-07-19.
 */
public class TaskWorkflowInclusiveDecisionFieldImpl extends ComboBox implements TaskWorkflowInclusiveDecisionField {

	public TaskWorkflowInclusiveDecisionFieldImpl() {
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
