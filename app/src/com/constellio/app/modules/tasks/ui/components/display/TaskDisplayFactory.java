package com.constellio.app.modules.tasks.ui.components.display;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.app.modules.tasks.TaskConfigs;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.ui.entities.TaskFollowerVO;
import com.constellio.app.modules.tasks.ui.entities.TaskReminderVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.display.ReferenceActiveLink;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsDisplayImpl;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsEditorImpl;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.StructureFactory;
import com.vaadin.ui.Component;

import static com.constellio.app.modules.tasks.model.wrappers.Task.LINKED_DOCUMENTS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.LINKED_FOLDERS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.REMINDERS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.REMINDER_FREQUENCY;
import static com.constellio.app.modules.tasks.model.wrappers.Task.TASK_FOLLOWERS;

public class TaskDisplayFactory extends MetadataDisplayFactory {
	@Override
	public Component buildSingleValue(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
		String metadataCode = metadata.getCode();
		String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
		if (TASK_FOLLOWERS.equals(metadataCode) || TASK_FOLLOWERS.equals(metadataCodeWithoutPrefix)) {
			return new TaskFollowerDisplay(toTaskFollowerVO((TaskFollower) displayValue));
		} else if (REMINDERS.equals(metadataCode) || REMINDERS.equals(metadataCodeWithoutPrefix)) {
			return new TaskReminderDisplay(toTaskReminderVO((TaskReminder) displayValue));
		} else if (REMINDER_FREQUENCY.equals(metadataCode) || REMINDER_FREQUENCY.equals(metadataCodeWithoutPrefix)) {
			return new TaskReminderFrequencyDisplay((String) displayValue);
		} else if (LINKED_DOCUMENTS.equals(metadataCode) || LINKED_DOCUMENTS.equals(metadataCodeWithoutPrefix)) {
			return new ReferenceActiveLink(RMNavigationConfiguration.DISPLAY_DOCUMENT, displayValue.toString());
		} else if (LINKED_FOLDERS.equals(metadataCode) || LINKED_FOLDERS.equals(metadataCodeWithoutPrefix)) {
			return new ReferenceActiveLink(RMNavigationConfiguration.DISPLAY_FOLDER, displayValue.toString());
		} else {
			return super.buildSingleValue(recordVO, metadata, displayValue);
		}
	}

	@Override
	public Component build(RecordVO recordVO, MetadataValueVO metadataValue) {
		MetadataVO metadataVO = metadataValue.getMetadata();
		String metadataCode = metadataVO.getCode();
		StructureFactory structureFactory = metadataVO.getStructureFactory();
		if (metadataVO.isMultivalue() && structureFactory != null && structureFactory instanceof CommentFactory) {
			Component displayComponent;
			if (Boolean.TRUE.equals(recordVO.getMetadataValue(recordVO.getMetadata(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode())).getValue())) {
				displayComponent = new RecordCommentsDisplayImpl(recordVO, metadataCode);
			} else {
				displayComponent = new RecordCommentsEditorImpl(recordVO, metadataCode) {
					TaskConfigs taskConfigs;

					@Override
					public boolean isAddButtonVisible() {
						String currentUsername = getSessionContext().getCurrentUser().getUsername();
						String currentCollection = getSessionContext().getCurrentCollection();
						User user = getConstellioFactories().getModelLayerFactory().newUserServices().getUserInCollection(currentUsername, currentCollection);
						Record record = getConstellioFactories().getModelLayerFactory().newRecordServices().getDocumentById(recordVO.getId());
						if (!user.hasWriteAccess().on(record)) {
							taskConfigs = new TaskConfigs(getConstellioFactories().getModelLayerFactory().getSystemConfigurationsManager());
							return taskConfigs.isAddCommentsWhenReadAuthorization();
						} else {
							return super.isAddButtonVisible();
						}
					}

					@Override
					public boolean isEditButtonVisible(Comment comment) {
						String currentUsername = getSessionContext().getCurrentUser().getUsername();
						String currentCollection = getSessionContext().getCurrentCollection();
						User user = getConstellioFactories().getModelLayerFactory().newUserServices().getUserInCollection(currentUsername, currentCollection);
						Record record = getConstellioFactories().getModelLayerFactory().newRecordServices().getDocumentById(recordVO.getId());
						if (!user.hasWriteAccess().on(record)) {
							taskConfigs = new TaskConfigs(getConstellioFactories().getModelLayerFactory().getSystemConfigurationsManager());
							return taskConfigs.isAddCommentsWhenReadAuthorization() && comment.getUserId().equals(getSessionContext().getCurrentUser().getId());
						} else {
							return comment.getUserId().equals(getSessionContext().getCurrentUser().getId());
						}
					}

					@Override
					public boolean isDeleteButtonVisible(Comment comment) {
						String currentUsername = getSessionContext().getCurrentUser().getUsername();
						String currentCollection = getSessionContext().getCurrentCollection();
						User user = getConstellioFactories().getModelLayerFactory().newUserServices().getUserInCollection(currentUsername, currentCollection);
						Record record = getConstellioFactories().getModelLayerFactory().newRecordServices().getDocumentById(recordVO.getId());
						if (!user.hasWriteAccess().on(record)) {
							taskConfigs = new TaskConfigs(getConstellioFactories().getModelLayerFactory().getSystemConfigurationsManager());
							return taskConfigs.isAddCommentsWhenReadAuthorization() && comment.getUserId().equals(getSessionContext().getCurrentUser().getId());
						} else {
							return super.isDeleteButtonVisible(comment);
						}
					}

					@Override
					public boolean isUserHasToHaveWriteAuthorization() {
						taskConfigs = new TaskConfigs(getConstellioFactories().getModelLayerFactory().getSystemConfigurationsManager());
						return !taskConfigs.isAddCommentsWhenReadAuthorization();
					}
				};
			}
			displayComponent.setWidthUndefined();
			return displayComponent;
		} else {
			return super.build(recordVO, metadataValue);
		}
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
}



