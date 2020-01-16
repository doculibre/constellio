package com.constellio.app.modules.tasks.services;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.ui.builders.TaskFollowerFromVOBuilder;
import com.constellio.app.modules.tasks.ui.builders.TaskReminderFromVOBuilder;
import com.constellio.app.modules.tasks.ui.entities.TaskFollowerVO;
import com.constellio.app.modules.tasks.ui.entities.TaskReminderVO;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.list.TaskCollaboratorItem;
import com.constellio.app.ui.framework.components.fields.list.TaskCollaboratorsGroupItem;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.tasks.model.wrappers.Task.TASK_COLLABORATORS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.TASK_COLLABORATORS_GROUPS;

public class TaskPresenterServices {
	private static Logger LOGGER = LoggerFactory.getLogger(TaskPresenterServices.class);
	final private TasksSchemasRecordsServices tasksSchemas;
	private final RecordServices recordServices;
	private final TasksSearchServices tasksSearchServices;
	private LoggingServices loggingServices;
	private SearchServices searchServices;

	public TaskPresenterServices(TasksSchemasRecordsServices tasksSchemas, RecordServices recordServices,
								 TasksSearchServices tasksSearchServices, LoggingServices loggingServices) {
		this.tasksSchemas = tasksSchemas;
		this.recordServices = recordServices;
		this.tasksSearchServices = tasksSearchServices;
		this.loggingServices = loggingServices;
		this.searchServices = tasksSchemas.appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	public Task toTask(TaskVO taskVO, Record record) {
		Task task = tasksSchemas.wrapTask(record);
		if(taskVO.getMetadataCodes().contains(taskVO.getSchema().getCode() + "_" + Task.REMINDERS)) {
			List<TaskReminderVO> remindersVOs = taskVO.getReminders();
			if (remindersVOs != null) {
				List<TaskReminder> reminders = new ArrayList<>();
				TaskReminderFromVOBuilder reminderBuilder = new TaskReminderFromVOBuilder();
				for (Object reminderVO : remindersVOs) {
					//FIXME should be VO!
					if (reminderVO instanceof TaskReminderVO) {
						reminders.add(reminderBuilder.build((TaskReminderVO) reminderVO));
					} else {
						reminders.add((TaskReminder) reminderVO);
					}
				}
				task.setReminders(reminders);
			}
		}
		if(taskVO.getMetadataCodes().contains(taskVO.getSchema().getCode() + "_" + Task.TASK_FOLLOWERS)) {
			List<TaskFollowerVO> followersVOs = taskVO.getTaskFollowers();
			if (taskVO.getTaskFollowers() != null) {
				TaskFollowerFromVOBuilder followerBuilder = new TaskFollowerFromVOBuilder();
				Map<String, TaskFollower> taskFollowersMap = new HashMap<>();
				for (Object follower : followersVOs) {
					TaskFollowerVO followerVO;
					//FIXME should always be VO
					if (follower instanceof TaskFollowerVO) {
						followerVO = (TaskFollowerVO) follower;
					} else {
						followerVO = toTaskFollowerVO((TaskFollower) follower);
					}
					TaskFollower currentTaskFollower = taskFollowersMap.get(followerVO.getFollowerId());
					if (currentTaskFollower == null) {
						currentTaskFollower = followerBuilder.build(followerVO);
					}
					if (followerVO.isFollowSubTasksModified()) {
						currentTaskFollower.setFollowSubTasksModified(true);
					}
					if (followerVO.isFollowTaskAssigneeModified()) {
						currentTaskFollower.setFollowTaskAssigneeModified(true);
					}
					if (followerVO.isFollowTaskCompleted()) {
						currentTaskFollower.setFollowTaskCompleted(true);
					}
					if (followerVO.isFollowTaskDeleted()) {
						currentTaskFollower.setFollowTaskDeleted(true);
					}
					if (followerVO.isFollowTaskStatusModified()) {
						currentTaskFollower.setFollowTaskStatusModified(true);
					}
					taskFollowersMap.put(currentTaskFollower.getFollowerId(), currentTaskFollower);
				}
				task.setTaskFollowers(new ArrayList<>(taskFollowersMap.values()));
			}
		}

		return task;
	}

	private TaskFollowerVO toTaskFollowerVO(TaskFollower taskFollower) {
		return new TaskFollowerVO(taskFollower.getFollowerId(), taskFollower.getFollowTaskAssigneeModified(),
				taskFollower.getFollowSubTasksModified(), taskFollower.getFollowTaskStatusModified(),
				taskFollower.getFollowTaskCompleted(), taskFollower.getFollowTaskDeleted());
	}

	public boolean isTaskOverdue(TaskVO task) {
		LocalDate dueDate = task.getDueDate();
		if (dueDate == null) {
			return false;
		}
		if (dueDate.isBefore(TimeProvider.getLocalDate())) {
			LocalDate endDate = task.getEndDate();
			if (endDate == null || endDate.isAfter(dueDate)) {
				return true;
			}
		}
		return false;
	}

	public boolean isEditTaskButtonVisible(Record record, User currentUser) {
		return currentUser.hasWriteAccess().on(record);
	}

	public boolean isCompleteTaskButtonVisible(Record record, User user) {
		if (!user.hasWriteAccess().on(record)) {
			return false;
		}
		if (!isAssignedToUser(record, user)) {
			return false;
		}
		Object statusId = record.get(tasksSchemas.userTask.status());
		if (statusId == null) {
			return true;
		}
		TaskStatus taskStatus = tasksSchemas.getTaskStatus((String) statusId);
		return taskStatus.isBeforeFinished();
	}

	public void closeTask(Record record, User user) {
		TaskStatus closeStatus = tasksSearchServices.getClosedStatus();
		addOrUpdate(record.set(tasksSchemas.userTask.status(), closeStatus), user);
	}

	public boolean isCloseTaskButtonVisible(Record record, User user) {
		if (!user.hasWriteAccess().on(record)) {
			return false;
		}
		if (!isAssignedToUser(record, user) && !wasCreatedByUser(record, user)) {
			return false;
		}
		Object statusId = record.get(tasksSchemas.userTask.status());
		if (statusId == null) {
			return false;
		}
		TaskStatus taskStatus = tasksSchemas.getTaskStatus((String) statusId);
		return taskStatus.isFinished();
	}

	boolean isAssignedToUser(Record record, User user) {
		Task task = tasksSchemas.wrapTask(record);
		if (task.getAssignee() != null && task.getAssignee().equals(user.getId())) {
			return true;
		}
		if (task.getAssigneeUsersCandidates() != null && task.getAssigneeUsersCandidates().contains(user.getId())) {
			return true;
		}
		return CollectionUtils.containsAny(task.getAssigneeGroupsCandidates(), user.getUserGroups());
	}

	boolean wasCreatedByUser(Record record, User user) {
		Task task = tasksSchemas.wrapTask(record);
		return task.getCreatedBy() != null && task.getCreatedBy().equals(user.getId());
	}

	public boolean isReadByUser(Record record) {
		Task task = tasksSchemas.wrapTask(record);
		return BooleanUtils.isTrue(task.getReadByUser());
	}

	public void setReadByUser(Record record, boolean readByUser) throws RecordServicesException {
		Task task = tasksSchemas.wrapTask(record);
		task.setReadByUser(readByUser);

		recordServices.update(task.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions().setSkipUSRMetadatasRequirementValidations(true));
	}

	public void sendReminder(Record record, User user) {
		Task task = tasksSchemas.wrapTask(record);
		List<TaskReminder> newReminders = new ArrayList<>(task.getReminders());
		newReminders.add(new TaskReminder().setFixedDate(TimeProvider.getLocalDate()));
		addOrUpdate(task.setReminders(newReminders).getWrappedRecord(), user);
	}

	public boolean isSendReminderButtonVisible(Record record, User user) {
		return user.hasWriteAccess().on(record);
	}


	public boolean isSubTaskPresentAndHaveCertainStatus(String id) {

		List<Record> tasksSearchServices = searchServices.search(new LogicalSearchQuery(
				LogicalSearchQueryOperators.from(tasksSchemas.taskSchemaType())
						.where(tasksSchemas.userTask.parentTask()).isEqualTo(id)));

		boolean isSubTaskWithRequiredStatusFound = false;

		for (Record taskAsRecord : tasksSearchServices) {
			Task currentTask = tasksSchemas.wrapTask(taskAsRecord);
			if (TaskStatusType.STANDBY.getCode().equalsIgnoreCase(currentTask.getStatusType().getCode())
				|| TaskStatusType.IN_PROGRESS.getCode().equalsIgnoreCase(currentTask.getStatusType().getCode())) {
				isSubTaskWithRequiredStatusFound = true;
				break;
			}
		}
		return isSubTaskWithRequiredStatusFound;
	}

	public void deleteTask(Record record, User currentUser) {

		recordServices.logicallyDelete(record, currentUser);
		//recordServices.physicallyDelete(record, currentUser);
	}

	public boolean isDeleteTaskButtonVisible(Record record, User user) {
		return user.hasDeleteAccess().on(record);
	}

	public final List<BatchProcess> addOrUpdate(Record record, User user) {
		return addOrUpdate(record, user, null);
	}

	public final List<BatchProcess> addOrUpdate(Record record, User user, RecordUpdateOptions updateOptions) {
		Transaction createTransaction = new Transaction();
		createTransaction.setUser(user);
		createTransaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		if (updateOptions != null) {
			createTransaction.setOptions(updateOptions);
		}
		createTransaction.addUpdate(record);
		try {
			return recordServices.executeHandlingImpactsAsync(createTransaction);
		} catch (RecordServicesException e) {
			Exception nestedException;
			if (e instanceof RecordServicesException.ValidationException) {
				LOGGER.error(e.getMessage(), e);
				nestedException = new ValidationException(((RecordServicesException.ValidationException) e).getErrors());
			} else {
				nestedException = e;
			}
			throw new RuntimeException(nestedException);
		}
	}

	public boolean isFinished(TaskVO taskVO) {
		TaskStatus status = tasksSchemas.getTaskStatus(taskVO.getStatus());
		return status.isFinished();
	}

	public void autoAssignTask(Record record, User user) {
		if (record == null) {
			return;
		}

		MetadataSchema schema = tasksSchemas.schema(record.getSchemaCode());

		record.set(schema.getMetadata(Task.ASSIGNEE), user.getId());
		record.set(schema.getMetadata(Task.ASSIGNER), user.getId());
		record.set(schema.getMetadata(Task.ASSIGNED_ON), TimeProvider.getLocalDate());
		addOrUpdate(record, user, RecordUpdateOptions.userModificationsSafeOptions());
	}

	public boolean isAutoAssignButtonEnabled(Record record, User user) {
		if (!user.hasWriteAccess().on(record)) {
			return false;
		}
		if (record == null) {
			return false;
		}
		Task task = tasksSchemas.getTask(record.getId());
		return task.getAssignee() == null && (task.getAssigneeUsersCandidates() == null || task.getAssigneeUsersCandidates()
				.isEmpty()) && (task.getAssigneeGroupsCandidates() == null || task.getAssigneeGroupsCandidates().isEmpty());
	}

	public boolean currentUserIsCollaborator(RecordVO recordVO, String currentUserId) {
		Record currentUserRecord = tasksSchemas.getAppLayerFactory().getModelLayerFactory().newRecordServices().getDocumentById(currentUserId);
		if (((List) recordVO.get(TASK_COLLABORATORS)).contains(currentUserId)) {
			return true;
		} else {
			return !Collections.disjoint(tasksSchemas.wrapUser(currentUserRecord).getUserGroups(), recordVO.get(TASK_COLLABORATORS_GROUPS));
		}
	}

	public void modifyCollaborators(List<TaskCollaboratorItem> taskCollaboratorItems,
									List<TaskCollaboratorsGroupItem> taskCollaboratorsGroupItems, RecordVO recordVO,
									SchemaPresenterUtils schemaPresenterUtils) {
		List<String> taskCollaborators = new ArrayList<>();
		List<Boolean> taskCollaboratorsWriteAuthorizations = new ArrayList<>();
		List<String> taskCollaboratorsGroups = new ArrayList<>();
		List<Boolean> taskCollaboratorsGroupsWriteAuthorizations = new ArrayList<>();
		for (TaskCollaboratorItem taskCollaboratorItem : taskCollaboratorItems) {
			taskCollaborators.add(taskCollaboratorItem.getTaskCollaborator());
			taskCollaboratorsWriteAuthorizations.add(taskCollaboratorItem.isTaskCollaboratorsWriteAuthorization());
		}
		for (TaskCollaboratorsGroupItem taskCollaboratorsGroupItem : taskCollaboratorsGroupItems) {
			taskCollaboratorsGroups.add(taskCollaboratorsGroupItem.getTaskCollaboratorGroup());
			taskCollaboratorsGroupsWriteAuthorizations.add(taskCollaboratorsGroupItem.isTaskCollaboratorGroupWriteAuthorization());
		}
		TaskVO taskVO = (TaskVO) recordVO;
		taskVO.setTaskCollaborators(taskCollaborators);
		taskVO.setTaskCollaboratorsWriteAuthorizations(taskCollaboratorsWriteAuthorizations);
		taskVO.setTaskCollaboratorsGroups(taskCollaboratorsGroups);
		taskVO.settaskCollaboratorsGroupsWriteAuthorizations(taskCollaboratorsGroupsWriteAuthorizations);

		Record record = schemaPresenterUtils.toRecord(taskVO);
		Task task = tasksSchemas.wrapTask(record);
		try {
			recordServices.update(task);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}
}
