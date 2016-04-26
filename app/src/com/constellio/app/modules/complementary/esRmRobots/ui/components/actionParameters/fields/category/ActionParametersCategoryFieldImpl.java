package com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.category;

import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.vaadin.data.Property;

public class ActionParametersCategoryFieldImpl extends LookupRecordField implements ActionParametersCategoryField {

	public ActionParametersCategoryFieldImpl() {
		super(Category.SCHEMA_TYPE);
	}

	@Override
	public String getFieldValue() {
		return getInternalValue();
	}

	@Override
	public void setFieldValue(String value) {
		setInternalValue(value);
	}

	@Override
	public void addValueChangeListener(final CategoryValueChangeListener listener) {
		addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
				String newValue = (String) event.getProperty().getValue();
				listener.valueChanged(newValue);
			}
		});
	}

}
