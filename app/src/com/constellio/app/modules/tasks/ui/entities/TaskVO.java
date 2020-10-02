package com.constellio.app.modules.tasks.ui.entities;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;
import org.joda.time.LocalDate;

import java.util.List;

import static com.constellio.app.modules.rm.wrappers.Document.IS_MODEL;
import static com.constellio.app.modules.tasks.model.wrappers.Task.ASSIGNEE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.ASSIGNEE_GROUPS_CANDIDATES;
import static com.constellio.app.modules.tasks.model.wrappers.Task.ASSIGNEE_USERS_CANDIDATES;
import static com.constellio.app.modules.tasks.model.wrappers.Task.DESCRIPTION;
import static com.constellio.app.modules.tasks.model.wrappers.Task.DUE_DATE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.END_DATE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.REMINDERS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.REMINDER_FREQUENCY;
import static com.constellio.app.modules.tasks.model.wrappers.Task.STATUS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.TASK_COLLABORATORS_GROUPS_WRITE_AUTHORIZATIONS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.TASK_COLLABORATORS_WRITE_AUTHORIZATIONS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.TASK_FOLLOWERS;

public class TaskVO extends RecordVO {

	public TaskVO(RecordVO recordVO) {
		super(recordVO.getId(), recordVO.getMetadataValues(), recordVO.getViewMode());
	}

	public TaskVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode, List<String> excludedMetadata) {
		super(id, metadataValues, viewMode, excludedMetadata);
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

	public Boolean isTaskModel() {
		return get(IS_MODEL);
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

	public TaskVO setReminderFrequency(String reminderFrequency) {
		set(REMINDER_FREQUENCY, reminderFrequency);
		return this;
	}

	public String getReminderFrequency() {
		return get(REMINDER_FREQUENCY);
	}

	public TaskVO setTaskCollaborators(List<String> taskCollaborators) {
		set(Task.TASK_COLLABORATORS, taskCollaborators);
		return this;
	}

	public TaskVO setTaskCollaboratorsWriteAuthorizations(List<Boolean> taskCollaboratorsWriteAuthorizations) {
		set(TASK_COLLABORATORS_WRITE_AUTHORIZATIONS, taskCollaboratorsWriteAuthorizations);
		return this;
	}

	public TaskVO setTaskCollaboratorsGroups(List<String> taskCollaborators) {
		set(Task.TASK_COLLABORATORS_GROUPS, taskCollaborators);
		return this;
	}

	public TaskVO settaskCollaboratorsGroupsWriteAuthorizations(List<Boolean> taskCollaboratorsWriteAuthorizations) {
		set(TASK_COLLABORATORS_GROUPS_WRITE_AUTHORIZATIONS, taskCollaboratorsWriteAuthorizations);
		return this;
	}
}
