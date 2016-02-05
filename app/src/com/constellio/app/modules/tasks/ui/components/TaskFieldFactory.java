package com.constellio.app.modules.tasks.ui.components;

import static com.constellio.app.modules.rm.wrappers.Document.TYPE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.PROGRESS_PERCENTAGE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.REMINDERS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.TASK_FOLLOWERS;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.tasks.ui.components.fields.CustomTaskField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskProgressPercentageFieldImpl;
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
		String metadataCode = metadata.getCode();
		String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
		MetadataInputType inputType = metadata.getMetadataInputType();
		if (TYPE.equals(metadataCode) || TYPE.equals(metadataCodeWithoutPrefix)) {
			if (MetadataInputType.LOOKUP.equals(inputType)) {
				field = new TaskTypeFieldLookupImpl();
			} else if (MetadataInputType.RADIO_BUTTONS.equals(inputType)) {
				field = new TaskTypeFieldOptionGroupImpl();
			} else {
				field = new TaskTypeFieldComboBoxImpl();
			}
		} else if (TASK_FOLLOWERS.equals(metadataCode) || TASK_FOLLOWERS.equals(metadataCodeWithoutPrefix)) {
			field = new ListAddRemoveTaskFollowerField();
		} else if (REMINDERS.equals(metadataCode) || REMINDERS.equals(metadataCodeWithoutPrefix)) {
			field = new ListAddRemoveTaskReminderField();
		} else if (PROGRESS_PERCENTAGE.equals(metadataCode) || PROGRESS_PERCENTAGE.equals(metadataCodeWithoutPrefix)) {
			field = new TaskProgressPercentageFieldImpl();
		} else {
			field = super.build(metadata);
		}
		if (field instanceof CustomTaskField) {
			postBuild(field, metadata);
		}
		return field;
	}

}
