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
