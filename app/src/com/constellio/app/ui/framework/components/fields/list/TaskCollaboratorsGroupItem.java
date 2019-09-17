package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;

import java.io.Serializable;

public class TaskCollaboratorsGroupItem implements Serializable {
	String taskCollaboratorGroup;
	boolean taskCollaboratorGroupWriteAuthorization;

	private RecordIdToCaptionConverter idToCaptionConverter = new RecordIdToCaptionConverter();

	public TaskCollaboratorsGroupItem(String taskCollaboratorGroup,
									  Boolean collaboratorWriteAuthorization) {
		this.taskCollaboratorGroup = taskCollaboratorGroup;
		this.taskCollaboratorGroupWriteAuthorization = collaboratorWriteAuthorization;
	}

	public TaskCollaboratorsGroupItem() {
	}

	public String getTaskCollaboratorGroup() {
		return taskCollaboratorGroup;
	}

	public void setTaskCollaboratorGroup(String taskCollaboratorGroup) {
		this.taskCollaboratorGroup = taskCollaboratorGroup;
	}

	public boolean isTaskCollaboratorGroupWriteAuthorization() {
		return taskCollaboratorGroupWriteAuthorization;
	}

	public void setCollaboratorReadAuthorization(boolean collaboratorReadAuthorization) {
		this.taskCollaboratorGroupWriteAuthorization = collaboratorReadAuthorization;
	}
}
