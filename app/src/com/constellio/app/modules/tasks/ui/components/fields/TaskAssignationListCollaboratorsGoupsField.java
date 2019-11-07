package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.ui.framework.components.fields.list.TaskCollaboratorsGroupItem;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.model.entities.records.wrappers.Group;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.modules.tasks.ui.components.fields.AuthorizationFieldItem.READ;
import static com.constellio.app.modules.tasks.ui.components.fields.AuthorizationFieldItem.WRITE;
import static com.constellio.app.ui.i18n.i18n.$;

public class TaskAssignationListCollaboratorsGoupsField extends CustomField<TaskCollaboratorsGroupItem> {

	private TaskCollaboratorsGroupItem taskCollaboratorsGroupItem;

	private I18NHorizontalLayout mainLayout;

	private LookupField<String> lookupGroupField;

	private OptionGroup authorizationField;

	private boolean writeButtonVisible;

	public TaskAssignationListCollaboratorsGoupsField(boolean writeButtonVisible) {
		this.writeButtonVisible = writeButtonVisible;
	}

	@Override
	protected Component initContent() {
		mainLayout = new I18NHorizontalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);

		authorizationField = new OptionGroup();
		authorizationField.addItem(READ);
		authorizationField.setItemCaption(READ, $("TaskAssignationListCollaboratorsField.collaboratorReadAuthorization"));

		if (writeButtonVisible) {
			authorizationField.addItem(WRITE);
			authorizationField.setItemCaption(WRITE, $("TaskAssignationListCollaboratorsField.collaboratorWriteAuthorization"));
		} else {
			authorizationField.setValue(READ);
		}
		authorizationField.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);

		lookupGroupField = new LookupRecordField(Group.SCHEMA_TYPE);

		if (taskCollaboratorsGroupItem != null) {
			authorizationField.setValue(taskCollaboratorsGroupItem.isTaskCollaboratorGroupWriteAuthorization() ? WRITE : READ);
			lookupGroupField.setValue(taskCollaboratorsGroupItem.getTaskCollaboratorGroup());
			taskCollaboratorsGroupItem = null;
		}

		mainLayout.addComponents(lookupGroupField, authorizationField);

		return mainLayout;
	}

	@Override
	public Object getConvertedValue() {
		Object convertedValue;
		Boolean writeAuthorization = WRITE.equals(authorizationField.getValue());
		String groupId = (String) lookupGroupField.getValue();
		if (writeAuthorization != null && groupId != null) {
			convertedValue = new TaskCollaboratorsGroupItem(groupId, writeAuthorization);
		} else {
			convertedValue = null;
		}
		return convertedValue;
	}

	@Override
	protected void setValue(TaskCollaboratorsGroupItem newFieldValue, boolean repaintIsNotNeeded,
							boolean ignoreReadOnly)
			throws ReadOnlyException, ConversionException, InvalidValueException {
		if (authorizationField != null && lookupGroupField != null) {
			Boolean newWriteAuthorization;
			String newGroupId;
			if (newFieldValue != null) {
				newGroupId = newFieldValue.getTaskCollaboratorGroup();
				newWriteAuthorization = newFieldValue.isTaskCollaboratorGroupWriteAuthorization();
			} else {
				newWriteAuthorization = null;
				newGroupId = null;
			}
			if (newWriteAuthorization != null) {
				authorizationField.setValue(newWriteAuthorization ? WRITE : READ);
			} else {
				authorizationField.setValue(null);
			}
			lookupGroupField.setValue(newGroupId);
		} else {
			taskCollaboratorsGroupItem = newFieldValue;
		}
	}

	@Override
	public Class<? extends TaskCollaboratorsGroupItem> getType() {
		return TaskCollaboratorsGroupItem.class;
	}

	public OptionGroup getAuthorizationField() {
		return authorizationField;
	}

}
