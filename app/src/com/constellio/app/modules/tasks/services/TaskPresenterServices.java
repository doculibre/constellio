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
package com.constellio.app.modules.tasks.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.ui.builders.TaskFollowerFromVOBuilder;
import com.constellio.app.modules.tasks.ui.builders.TaskReminderFromVOBuilder;
import com.constellio.app.modules.tasks.ui.entities.TaskFollowerVO;
import com.constellio.app.modules.tasks.ui.entities.TaskReminderVO;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;

public class TaskPresenterServices {
	private static Logger LOGGER = LoggerFactory.getLogger(TaskPresenterServices.class);
	final private TasksSchemasRecordsServices tasksSchemas;
	private final RecordServices recordServices;
	private final TasksSearchServices tasksSearchServices;
	private LoggingServices loggingServices;

	public TaskPresenterServices(TasksSchemasRecordsServices tasksSchemas, RecordServices recordServices,
			TasksSearchServices tasksSearchServices, LoggingServices loggingServices) {
		this.tasksSchemas = tasksSchemas;
		this.recordServices = recordServices;
		this.tasksSearchServices = tasksSearchServices;
		this.loggingServices = loggingServices;
	}

	public Task toTask(TaskVO taskVO, Record record) {
		Task task = tasksSchemas.wrapTask(record);
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
		return task;
	}

	private TaskFollowerVO toTaskFollowerVO(TaskFollower taskFollower) {
		return new TaskFollowerVO(taskFollower.getFollowerId(), taskFollower.getFollowTaskAssigneeModified(),
				taskFollower.getFollowSubTasksModified(), taskFollower.getFollowTaskStatusModified(),
				taskFollower.getFollowTaskCompleted(), taskFollower.getFollowTaskDeleted());
	}

	public boolean isTaskOverDue(TaskVO task) {
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
		if (!isAssignedToUser(record, user)) {
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

	public void sendReminder(Record record, User user) {
		Task task = tasksSchemas.wrapTask(record);
		List<TaskReminder> newReminders = new ArrayList<>(task.getReminders());
		newReminders.add(new TaskReminder().setFixedDate(TimeProvider.getLocalDate()));
		addOrUpdate(task.setReminders(newReminders).getWrappedRecord(), user);
	}

	public boolean isSendReminderButtonVisible(Record record, User user) {
		return user.hasWriteAccess().on(record);
	}

	public void deleteTask(Record record, User currentUser) {
		recordServices.logicallyDelete(record, currentUser);
		loggingServices.logDeleteRecordWithJustification(record, currentUser, "");
		recordServices.physicallyDelete(record, currentUser);
	}

	public boolean isDeleteTaskButtonVisible(Record record, User user) {
		return user.hasDeleteAccess().on(record);
	}

	public final List<BatchProcess> addOrUpdate(Record record, User user) {
		Transaction createTransaction = new Transaction();
		createTransaction.setUser(user);
		createTransaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
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
		record.set(tasksSchemas.userTask.assignee(), user.getId());
		record.set(tasksSchemas.userTask.assigner(), user.getId());
		record.set(tasksSchemas.userTask.assignedOn(), TimeProvider.getLocalDate());
		addOrUpdate(record, user);
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
}
