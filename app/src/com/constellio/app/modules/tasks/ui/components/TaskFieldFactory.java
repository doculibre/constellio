/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.tasks.ui.components;

import static com.constellio.app.modules.tasks.model.wrappers.Task.PROGRESS_PERCENTAGE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.REMINDERS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.TASK_FOLLOWERS;

import com.constellio.app.modules.tasks.ui.components.fields.CustomTaskField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskProgressPercentageFieldImpl;
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
		if (TASK_FOLLOWERS.equals(metadataCode) || TASK_FOLLOWERS.equals(metadataCodeWithoutPrefix)) {
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
