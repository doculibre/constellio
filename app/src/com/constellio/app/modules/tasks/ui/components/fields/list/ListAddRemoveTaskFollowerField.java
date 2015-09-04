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
package com.constellio.app.modules.tasks.ui.components.fields.list;

import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.ui.components.converters.TaskFollowerVOToStringConverter;
import com.constellio.app.modules.tasks.ui.components.fields.TaskFollowerFieldImpl;
import com.constellio.app.modules.tasks.ui.entities.TaskFollowerVO;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;

@SuppressWarnings("unchecked")
public class ListAddRemoveTaskFollowerField extends ListAddRemoveField<TaskFollowerVO, TaskFollowerFieldImpl> {

	private TaskFollowerVOToStringConverter converter = new TaskFollowerVOToStringConverter();

	@Override
	protected TaskFollowerFieldImpl newAddEditField() {
		return new TaskFollowerFieldImpl();
	}

	//FIXME should be always Vo or not
	@Override
	protected String getItemCaption(Object itemId) {
		if (itemId instanceof TaskFollowerVO) {
			return converter.convertToPresentation((TaskFollowerVO) itemId, String.class, getLocale());
		} else {
			return converter.convertToPresentation(toTaskFollowerVO((TaskFollower) itemId), String.class, getLocale());
		}
	}

	private TaskFollowerVO toTaskFollowerVO(TaskFollower taskFollower) {
		return new TaskFollowerVO(taskFollower.getFollowerId(), taskFollower.getFollowTaskAssigneeModified(),
				taskFollower.getFollowSubTasksModified(), taskFollower.getFollowTaskStatusModified(),
				taskFollower.getFollowTaskCompleted(), taskFollower.getFollowTaskDeleted());
	}

}
