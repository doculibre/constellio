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
package com.constellio.app.modules.tasks.model.wrappers;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class Task extends RecordWrapper {
	public static final String SCHEMA_TYPE = "userTask";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String TYPE = "type";
	public static final String ASSIGNEE = "assignee";
	public static final String ASSIGNER = "assigner";
	public static final String ASSIGNEE_USERS_CANDIDATES = "assigneeUsersCandidates";
	public static final String ASSIGNEE_GROUPS_CANDIDATES = "assigneeGroupsCandidates";
	public static final String ASSIGNED_ON = "assignedOn";
	public static final String FOLLOWERS_IDS = "taskFollowersIds";
	public static final String TASK_FOLLOWERS = "taskFollowers";
	public static final String DESCRIPTION = "description";

	//AFTER : Rename to CONTENTS
	public static final String CONTENT = "contents";
	public static final String NEXT_REMINDER_ON = "nextReminderOn";
	public static final String REMINDERS = "reminders";
	public static final String START_DATE = "startDate";
	public static final String DUE_DATE = "dueDate";
	public static final String END_DATE = "endDate";
	public static final String STATUS = "status";
	public static final String PROGRESS_PERCENTAGE = "progressPercentage";
	public static final String PARENT_TASK = "parentTask";
	public static final String PARENT_TASK_DUE_DATE = "parentTaskDueDate";
	public static final String COMMENTS = "comments";

	public Task(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getAssignee() {
		return get(ASSIGNEE);
	}

	public String getAssigner() {
		return get(ASSIGNER);
	}

	public Task setAssignee(String assignedTo) {
		set(ASSIGNEE, assignedTo);
		return this;
	}

	public Task setAssigner(String assignedBy) {
		set(ASSIGNER, assignedBy);
		return this;
	}

	public List<String> getAssigneeUsersCandidates() {
		return get(ASSIGNEE_USERS_CANDIDATES);
	}

	public Task setAssigneeUsersCandidates(List<String> users) {
		set(ASSIGNEE_USERS_CANDIDATES, users);
		return this;
	}

	public List<String> getAssigneeGroupsCandidates() {
		return get(ASSIGNEE_GROUPS_CANDIDATES);
	}

	public Task setAssigneeGroupsCandidates(List<String> groups) {
		set(ASSIGNEE_GROUPS_CANDIDATES, groups);
		return this;
	}

	public LocalDate getAssignedOn() {
		return get(ASSIGNED_ON);
	}

	public List<String> getFollowersIds() {
		return get(FOLLOWERS_IDS);
	}

	public List<TaskFollower> getTaskFollowers() {
		return get(TASK_FOLLOWERS);
	}

	public Task setTaskFollowers(List<TaskFollower> taskFollowers) {
		set(TASK_FOLLOWERS, taskFollowers);
		return this;
	}

	public String getComments() {
		return get(COMMENTS);
	}

	public Task setComments(String comments) {
		set(COMMENTS, comments);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public Task setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public Content getContent() {
		return get(CONTENT);
	}

	public Task setContent(Content content) {
		set(CONTENT, content);
		return this;
	}

	public LocalDate getNextReminderOn() {
		return get(NEXT_REMINDER_ON);
	}

	public List<TaskReminder> getReminders() {
		return get(REMINDERS);
	}

	public Task setReminders(List<TaskReminder> reminders) {
		set(REMINDERS, reminders);
		return this;
	}

	public LocalDate getStartDate() {
		return get(START_DATE);
	}

	public Task setStartDate(LocalDate startDate) {
		set(START_DATE, startDate);
		return this;
	}

	public LocalDate getParentTaskDueDate() {
		return get(PARENT_TASK_DUE_DATE);
	}

	public LocalDate getDueDate() {
		return get(DUE_DATE);
	}

	public LocalDate getEndDate() {
		return get(END_DATE);
	}

	public Task setDueDate(LocalDate dueDate) {
		set(DUE_DATE, dueDate);
		return this;
	}

	public RecordWrapper setEndDate(LocalDate endDate) {
		set(END_DATE, endDate);
		return this;
	}

	public String getStatus() {
		return get(STATUS);
	}

	public Task setStatus(String status) {
		set(STATUS, status);
		return this;
	}

	public Double getProgressPercentage() {
		return get(PROGRESS_PERCENTAGE);
	}

	public Task setProgressPercentage(Double progress) {
		set(PROGRESS_PERCENTAGE, progress);
		return this;
	}

	public String getParentTask() {
		return get(PARENT_TASK);
	}

	public Task setParentTask(Task task) {
		set(PARENT_TASK, task);
		return this;
	}

	public Task setParentTask(Record task) {
		set(PARENT_TASK, task);
		return this;
	}

	public Task setParentTask(String task) {
		set(PARENT_TASK, task);
		return this;
	}

	public Task setAssignationDate(LocalDate date) {
		set(ASSIGNED_ON, date);
		return this;
	}

	public Task setTitle(String title) {
		super.setTitle(title);
		return this;
	}
}
