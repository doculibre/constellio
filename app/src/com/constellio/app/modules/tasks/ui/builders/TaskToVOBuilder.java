package com.constellio.app.modules.tasks.ui.builders;

import java.util.List;

import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.ui.entities.TaskFollowerVO;
import com.constellio.app.modules.tasks.ui.entities.TaskReminderVO;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

public class TaskToVOBuilder extends RecordToVOBuilder { 
	
	@Override
	protected Object getValue(Record record, Metadata metadata) {
		Object value = super.getValue(record, metadata);
		if (value != null) {
			if (value instanceof TaskReminder) {
				return toTaskReminderVO((TaskReminder) value);
			} else if (value instanceof TaskFollower) {
				return toTaskFollowerVO((TaskFollower) value);
			}
		}
		return value;
	}

	private TaskFollowerVO toTaskFollowerVO(TaskFollower taskFollower) {
		return new TaskFollowerVO(taskFollower.getFollowerId(), taskFollower.getFollowTaskAssigneeModified(),
				taskFollower.getFollowSubTasksModified(), taskFollower.getFollowTaskStatusModified(),
				taskFollower.getFollowTaskCompleted(), taskFollower.getFollowTaskDeleted());
	}

	private TaskReminderVO toTaskReminderVO(TaskReminder taskReminder) {
		return new TaskReminderVO(taskReminder.getFixedDate(), taskReminder.getNumberOfDaysToRelativeDate(),
				taskReminder.getRelativeDateMetadataCode(), taskReminder.isBeforeRelativeDate());
	}

	@Override
	public TaskVO build(Record record, VIEW_MODE viewMode) {
		return (TaskVO) super.build(record, viewMode);
	}

	@Override
	public TaskVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return (TaskVO) super.build(record, viewMode, sessionContext);
	}

	@Override
	public TaskVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO) {
		return (TaskVO) super.build(record, viewMode, schemaVO);
	}

	@Override
	protected TaskVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode) {
		return new TaskVO(id, metadataValueVOs, viewMode);
	}
	
}