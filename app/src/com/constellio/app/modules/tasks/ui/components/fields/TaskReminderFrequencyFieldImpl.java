package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.modules.tasks.services.background.AlertOverdueTasksBackgroundAction;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.components.fields.number.BaseIntegerField;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.*;
import org.joda.time.LocalDate;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class TaskReminderFrequencyFieldImpl extends CustomField<String> implements TaskReminderFrequencyField {
	public final static String PARAMETER_SEPARATOR = AlertOverdueTasksBackgroundAction.PARAMETER_SEPARATOR;

	ComboBox reminderFrequencyType;
	BaseIntegerField reminderFrequencyValue;

	ComboBox reminderDurationType;
	BaseIntegerField reminderDurationValue;
	JodaDateField reminderLimitDateValue;

	@Override
	protected Component initContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		VerticalLayout inputLayout = new VerticalLayout();
		inputLayout.setSpacing(true);
		HorizontalLayout frequencyLayout = new HorizontalLayout();
		frequencyLayout.setSpacing(true);
		reminderFrequencyType = new ComboBox(null, asList("mm", "hh", "DD"));
		reminderFrequencyType.setItemCaption("mm", $("Date.mm"));
		reminderFrequencyType.setItemCaption("hh", $("Date.hh"));
		reminderFrequencyType.setItemCaption("DD", $("Date.DD"));
		reminderFrequencyType.setNullSelectionAllowed(false);
		reminderFrequencyType.setValue("mm");
		reminderFrequencyValue = new BaseIntegerField();

		HorizontalLayout durationLayout = new HorizontalLayout();
		durationLayout.setSpacing(true);
		reminderDurationType = new ComboBox("", asList("Times", "Date"));
		reminderDurationType.setItemCaption("Times", $("TaskReminderFrequencyField.times"));
		reminderDurationType.setItemCaption("Date", $("Date.Date"));
		reminderDurationType.setNullSelectionAllowed(false);
		reminderDurationType.setValue("Times");
		reminderDurationValue = new BaseIntegerField($("TaskReminderFrequencyField.duration"));
		reminderLimitDateValue = new JodaDateField($("TaskReminderFrequencyField.duration"));
		reminderLimitDateValue.setVisible(false);

		initFieldsValues();
		initFieldsListeners();

		frequencyLayout.addComponents(reminderFrequencyValue, reminderFrequencyType);
		durationLayout.addComponents(reminderDurationValue, reminderLimitDateValue, reminderDurationType);
		inputLayout.addComponents(frequencyLayout, durationLayout);
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
		ValueChangeListener changeDurationTypeListener = new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				String value = (String) event.getProperty().getValue();
				switch (value) {
					case "Times":
						updateDurationType(false);
						break;
					case "Date":
						updateDurationType(true);
						break;
				}
			}
		};
		reminderFrequencyType.addValueChangeListener(valueChangeListener);
		reminderFrequencyValue.addValueChangeListener(valueChangeListener);
		reminderDurationType.addValueChangeListener(valueChangeListener);
		reminderDurationType.addValueChangeListener(changeDurationTypeListener);
		reminderDurationValue.addValueChangeListener(valueChangeListener);
		reminderLimitDateValue.addValueChangeListener(valueChangeListener);
	}

	@Override
	public Class<? extends String> getType() {
		return String.class;
	}

	@Override
	public Object getFieldValue() {
		String returnedValue = null;
		if(reminderFrequencyType.getValue() != null && reminderFrequencyValue.getValue() != null && !reminderFrequencyValue.getValue().isEmpty()) {
			returnedValue =  reminderFrequencyType.getValue() + PARAMETER_SEPARATOR + reminderFrequencyValue.getValue();
			if(reminderDurationType.getValue() != null) {
				String durationType = (String) reminderDurationType.getValue();
				if("Date".equals(durationType) && reminderLimitDateValue.getValue() != null) {
					returnedValue += PARAMETER_SEPARATOR + reminderDurationType.getValue() + PARAMETER_SEPARATOR + LocalDate.fromDateFields(reminderLimitDateValue.getValue()).toString();
				} else if(reminderDurationValue.getValue() != null && !reminderDurationValue.getValue().isEmpty()) {
					returnedValue += PARAMETER_SEPARATOR + reminderDurationType.getValue() + PARAMETER_SEPARATOR + reminderDurationValue.getValue();
				}
			}
		}
		return returnedValue;
	}

	@Override
	public void setFieldValue(Object value) {
		if(value != null) {
			String[] parameters = ((String) value).split(PARAMETER_SEPARATOR);
			if(parameters.length == 2) {
				reminderFrequencyType.setValue(parameters[0]);
				reminderFrequencyValue.setValue(parameters[1]);
			} else if(parameters.length == 4) {
				reminderFrequencyType.setValue(parameters[0]);
				reminderFrequencyValue.setValue(parameters[1]);
				String durationType = parameters[2];
				reminderDurationType.setValue(durationType);
				if("Date".equals(durationType)) {
					reminderLimitDateValue.setValue(LocalDate.parse(parameters[3]).toDate());
					updateDurationType(true);
				} else {
					reminderDurationValue.setValue(parameters[3]);
					updateDurationType(false);
				}
			}
		}
	}

	private void updateDurationType(boolean isDateType) {
		reminderLimitDateValue.setVisible(isDateType);
		reminderDurationValue.setVisible(!isDateType);
	}
}
