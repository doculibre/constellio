package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.ui.framework.components.fields.list.TaskCollaboratorItem;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class TaskAssignationListCollaboratorsField extends CustomField<TaskCollaboratorItem> {

	private TaskCollaboratorItem taskCollaboratorItem;

	private I18NHorizontalLayout mainLayout;

	private LookupField<String> lookupUserField;

	private OptionGroup authorizationField;

	@Override
	protected Component initContent() {
		mainLayout = new I18NHorizontalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);

		authorizationField = new OptionGroup("", asList(false, true));
		authorizationField.setItemCaption(false, $("TaskAssignationListCollaboratorsField.collaboratorReadAuthorization"));
		authorizationField.setItemCaption(true, $("TaskAssignationListCollaboratorsField.collaboratorWriteAuthorization"));
		authorizationField.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);

		lookupUserField = new LookupRecordField(User.SCHEMA_TYPE);
		lookupUserField.setCaption($("TaskAssignationListCollaboratorsField.taskCollaborators"));

		if (taskCollaboratorItem != null) {
			authorizationField.setValue(taskCollaboratorItem.isTaskCollaboratorsWriteAuthorization());
			lookupUserField.setValue(taskCollaboratorItem.getTaskCollaborator());
			taskCollaboratorItem = null;
		}

		mainLayout.addComponents(lookupUserField, authorizationField);

		return mainLayout;
	}

	@Override
	public Object getConvertedValue() {
		Object convertedValue;
		Boolean writeAuthorization = (Boolean) authorizationField.getValue();
		String userId = (String) lookupUserField.getValue();
		if (writeAuthorization != null && userId != null) {
			convertedValue = new TaskCollaboratorItem(userId, writeAuthorization);
		} else {
			convertedValue = null;
		}
		return convertedValue;
	}

	@Override
	protected void setValue(TaskCollaboratorItem newFieldValue, boolean repaintIsNotNeeded, boolean ignoreReadOnly)
			throws ReadOnlyException, ConversionException, InvalidValueException {
		if (authorizationField != null && lookupUserField != null) {
			Boolean newWriteAuthorization;
			String newUserId;
			if (newFieldValue != null) {
				newUserId = newFieldValue.getTaskCollaborator();
				newWriteAuthorization = newFieldValue.isTaskCollaboratorsWriteAuthorization();
			} else {
				newWriteAuthorization = null;
				newUserId = null;
			}
			authorizationField.setValue(newWriteAuthorization);
			lookupUserField.setValue(newUserId);
		} else {
			taskCollaboratorItem = newFieldValue;
		}
	}

	@Override
	public Class<? extends TaskCollaboratorItem> getType() {
		return TaskCollaboratorItem.class;
	}

}
