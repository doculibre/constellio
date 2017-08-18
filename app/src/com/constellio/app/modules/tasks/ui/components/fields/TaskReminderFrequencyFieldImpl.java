package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.ui.framework.components.fields.number.BaseIntegerField;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.*;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class TaskReminderFrequencyFieldImpl extends CustomField<String> implements TaskReminderFrequencyField {
	ComboBox reminderFrequencyType;
	BaseIntegerField reminderFrequencyValue;

	@Override
	protected Component initContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		HorizontalLayout inputLayout = new HorizontalLayout();
		inputLayout.setSpacing(true);
		reminderFrequencyType = new ComboBox("", asList("mm", "hh", "DD"));
		reminderFrequencyType.setItemCaption("mm", $("Date.mm"));
		reminderFrequencyType.setItemCaption("hh", $("Date.hh"));
		reminderFrequencyType.setItemCaption("DD", $("Date.DD"));
		reminderFrequencyType.setNullSelectionAllowed(false);
		reminderFrequencyType.setValue("mm");
		reminderFrequencyValue = new BaseIntegerField("");

		initFieldsValues();
		initFieldsListeners();

		inputLayout.addComponents(reminderFrequencyValue, reminderFrequencyType);
		mainLayout.addComponent(inputLayout);
		return mainLayout;
	}

	private void initFieldsValues() {
		if(getValue() != null) {
			setFieldValue(getValue());
		}
	}

	private void initFieldsListeners() {
		ValueChangeListener valueChangeListener = new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				TaskReminderFrequencyFieldImpl.this.setValue((String) getFieldValue());
			}
		};
		reminderFrequencyType.addValueChangeListener(valueChangeListener);
		reminderFrequencyValue.addValueChangeListener(valueChangeListener);
	}

	@Override
	public Class<? extends String> getType() {
		return String.class;
	}

	@Override
	public Object getFieldValue() {
		String returnedValue = null;
		if(reminderFrequencyType.getValue() != null && reminderFrequencyValue.getValue() != null && !reminderFrequencyValue.getValue().isEmpty()) {
			returnedValue =  reminderFrequencyType.getValue() + ":" + reminderFrequencyValue.getValue();
		}
		return returnedValue;
	}

	@Override
	public void setFieldValue(Object value) {
		if(value != null) {
			String[] parameters = ((String) value).split(":");
			if(parameters.length == 2) {
				reminderFrequencyType.setValue(parameters[0]);
				reminderFrequencyValue.setValue(parameters[1]);
			}
		}
	}
}
