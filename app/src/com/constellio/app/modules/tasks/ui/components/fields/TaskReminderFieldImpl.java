package com.constellio.app.modules.tasks.ui.components.fields;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Date;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.entities.TaskReminderVO;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class TaskReminderFieldImpl extends CustomField<TaskReminderVO> implements TaskReminderField {
	
	private TaskReminderVO taskReminderVO;
	
	private BeanItem<TaskReminderVO> taskReminderItem;
	
	private FieldGroup fieldGroup;
	
	@PropertyId("fixedDate")
	private JodaDateField fixedDateField;
	
	@PropertyId("relativeDateMetadataCode")
	private ComboBox relativeDateMetadataCodeField;

	@PropertyId("numberOfDaysToRelativeDate")
	private TextField numberOfDaysToRelativeDateField;
	
	private Label numberOfDaysToRelativeDateLabel;
	
	@PropertyId("beforeRelativeDate")
	private ComboBox beforeRelativeDateField;
	
	@Override
	protected Component initContent() {
		if (taskReminderVO == null) {
			taskReminderVO = new TaskReminderVO();
		}
		taskReminderItem = new BeanItem<>(taskReminderVO);
		fieldGroup = new FieldGroup(taskReminderItem);
		
		setPropertyDataSource(new AbstractProperty<TaskReminderVO>() {
			@Override
			public TaskReminderVO getValue() {
				boolean submittedValueValid = taskReminderVO.getFixedDate() != null || taskReminderVO.getRelativeDateMetadataCode() != null; 
				return  submittedValueValid ? taskReminderVO : null;
			}

			@Override
			public void setValue(TaskReminderVO newValue)
					throws com.vaadin.data.Property.ReadOnlyException {
				setInternalValue(newValue);
				taskReminderVO = newValue != null ? newValue : new TaskReminderVO();
				if (fieldGroup != null) {
					taskReminderItem = new BeanItem<>(taskReminderVO);
					fieldGroup.setItemDataSource(taskReminderItem);
				}
			}

			@Override
			public Class<? extends TaskReminderVO> getType() {
				return TaskReminderVO.class;
			}
		});

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("99%");
		mainLayout.setHeightUndefined();
		mainLayout.setSpacing(true);
		
		fixedDateField = new JodaDateField();
		fixedDateField.setCaption($("TaskReminderField.fixedDate"));
		fixedDateField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (event.getProperty().getValue() != null) {
					numberOfDaysToRelativeDateField.setValue("0");
					beforeRelativeDateField.setValue(null);
					relativeDateMetadataCodeField.setValue(null);
				}
			}
		});
		
		HorizontalLayout relativeDateLayout = new HorizontalLayout();
		relativeDateLayout.setCaption($("TaskReminderField.relativeDateMetadataCode"));
		relativeDateLayout.setSpacing(true);
		
		relativeDateMetadataCodeField = new ComboBox();
		relativeDateMetadataCodeField.addItem(Task.START_DATE);
		relativeDateMetadataCodeField.addItem(Task.DUE_DATE);
		relativeDateMetadataCodeField.setItemCaption(Task.START_DATE, $("TaskReminderField.startDate"));
		relativeDateMetadataCodeField.setItemCaption(Task.DUE_DATE, $("TaskReminderField.dueDate"));
		relativeDateMetadataCodeField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (event.getProperty().getValue() != null) {
					fixedDateField.setValue(null);
				}
			}
		});

		numberOfDaysToRelativeDateField = new TextField();
		numberOfDaysToRelativeDateField.setWidth(3, Unit.EM);
		numberOfDaysToRelativeDateField.setConverter(Integer.class);
		
		numberOfDaysToRelativeDateLabel = new Label($("TaskReminderField.numberOfDaysToRelativeDate"));
		
		beforeRelativeDateField = new ComboBox();
		beforeRelativeDateField.addItem(Boolean.TRUE);
		beforeRelativeDateField.addItem(Boolean.FALSE);
		beforeRelativeDateField.setItemCaption(Boolean.TRUE, $("TaskReminderField.beforeRelativeDate"));
		beforeRelativeDateField.setItemCaption(Boolean.FALSE, $("TaskReminderField.afterRelativeDate"));
		
		mainLayout.addComponents(fixedDateField, relativeDateLayout);
		relativeDateLayout.addComponents(numberOfDaysToRelativeDateField, numberOfDaysToRelativeDateLabel, beforeRelativeDateField, relativeDateMetadataCodeField);
		
		fieldGroup.bindMemberFields(this);

		return mainLayout;
	}

	@Override
	public Class<? extends TaskReminderVO> getType() {
		return TaskReminderVO.class;
	}
	
	private boolean isInvalidFieldValue() {
		boolean invalidFieldValue;
		
		Date fixedDateValue = fixedDateField.getValue();
		String relativeDateMetadataCodeValue = (String) relativeDateMetadataCodeField.getValue();
		
		if (fixedDateValue == null && relativeDateMetadataCodeValue == null) {
			invalidFieldValue = true;
		} else if (fixedDateValue != null && relativeDateMetadataCodeValue != null) {
			invalidFieldValue = true;
		} else {
			invalidFieldValue = false;
		}
		return invalidFieldValue;
	}

	@Override
	public void commit()
			throws SourceException, InvalidValueException {
		if (!isInvalidFieldValue()) {
			try {
				fieldGroup.commit();
			} catch (CommitException e) {
				throw new InvalidValueException(e.getMessage());
			}
			super.commit();
		} 
	}
	
}
