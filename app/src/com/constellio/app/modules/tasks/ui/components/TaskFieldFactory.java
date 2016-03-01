package com.constellio.app.modules.tasks.ui.components;

import static com.constellio.app.modules.rm.wrappers.Document.TYPE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.DECISION;
import static com.constellio.app.modules.tasks.model.wrappers.Task.PROGRESS_PERCENTAGE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.RELATIVE_DUE_DATE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.REMINDERS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.TASK_FOLLOWERS;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.tasks.ui.components.fields.CustomTaskField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskDecisionFieldImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskProgressPercentageFieldImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskRelativeDueDateFieldImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskTypeFieldComboBoxImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskTypeFieldLookupImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskTypeFieldOptionGroupImpl;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveTaskFollowerField;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveTaskReminderField;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.vaadin.ui.Field;

public class TaskFieldFactory extends MetadataFieldFactory {
	@Override
	public Field<?> build(MetadataVO metadata) {
		Field<?> field;
		MetadataInputType inputType = metadata.getMetadataInputType();
		switch (metadata.getLocalCode()) {
		case TYPE:
			if (MetadataInputType.LOOKUP.equals(inputType)) {
				field = new TaskTypeFieldLookupImpl();
			} else if (MetadataInputType.RADIO_BUTTONS.equals(inputType)) {
				field = new TaskTypeFieldOptionGroupImpl();
			} else {
				field = new TaskTypeFieldComboBoxImpl();
			}
			break;
		case TASK_FOLLOWERS:
			field = new ListAddRemoveTaskFollowerField();
			break;
		case REMINDERS:
			field = new ListAddRemoveTaskReminderField();
			break;
		case PROGRESS_PERCENTAGE:
			field = new TaskProgressPercentageFieldImpl();
			break;
		case RELATIVE_DUE_DATE:
			field = new TaskRelativeDueDateFieldImpl();
			break;
		case DECISION:
			field = new TaskDecisionFieldImpl();
			break;
		default:
			field = super.build(metadata);
		}
		if (field instanceof CustomTaskField) {
			postBuild(field, metadata);
		}
		return field;
	}
}
