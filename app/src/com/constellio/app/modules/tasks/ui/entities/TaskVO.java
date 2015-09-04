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
package com.constellio.app.modules.tasks.ui.entities;

import static com.constellio.app.modules.tasks.model.wrappers.Task.ASSIGNEE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.ASSIGNEE_GROUPS_CANDIDATES;
import static com.constellio.app.modules.tasks.model.wrappers.Task.ASSIGNEE_USERS_CANDIDATES;
import static com.constellio.app.modules.tasks.model.wrappers.Task.DESCRIPTION;
import static com.constellio.app.modules.tasks.model.wrappers.Task.DUE_DATE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.END_DATE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.REMINDERS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.STATUS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.TASK_FOLLOWERS;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;

public class TaskVO extends RecordVO {

	public TaskVO(RecordVO recordVO) {
		super(recordVO.getId(), recordVO.getMetadataValues(), recordVO.getViewMode());
	}

	public String getAssignee() {
		return get(ASSIGNEE);
	}

	public void setAssignee(String assignee) {
		set(ASSIGNEE, assignee);
	}

	public List<UserVO> getAssignationUsersCandidates() {
		return get(ASSIGNEE_USERS_CANDIDATES);
	}

	public void setAssignationUsersCandidates(List<UserVO> assignationUsersCandidates) {
		set(ASSIGNEE_USERS_CANDIDATES, assignationUsersCandidates);
	}

	public List<GlobalGroupVO> getAssignationGroupsCandidates() {
		return get(ASSIGNEE_GROUPS_CANDIDATES);
	}

	public void setAssignationGroupsCandidates(List<GlobalGroupVO> assignationGroupsCandidates) {
		set(ASSIGNEE_GROUPS_CANDIDATES, assignationGroupsCandidates);
	}

	public LocalDate getDueDate() {
		return get(DUE_DATE);
	}

	public void setDueDate(LocalDate dueDate) {
		set(DUE_DATE, dueDate);
	}

	public LocalDate getEndDate() {
		return get(END_DATE);
	}

	public void setEndDate(LocalDate endDate) {
		set(END_DATE, endDate);
	}

	public List<TaskReminderVO> getReminders() {
		return get(REMINDERS);
	}

	public void setReminders(List<TaskReminderVO> reminders) {
		set(REMINDERS, reminders);
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public void setDescription(String description) {
		set(DESCRIPTION, description);
	}

	public List<TaskFollowerVO> getTaskFollowers() {
		return get(TASK_FOLLOWERS);
	}

	public TaskVO setTaskFollowers(List<TaskFollowerVO> followers) {
		set(TASK_FOLLOWERS, followers);
		return this;
	}

	public String getStatus() {
		return get(STATUS);
	}
}
