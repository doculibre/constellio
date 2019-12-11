package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;

import java.io.Serializable;

public class TaskCollaboratorItem implements Serializable {
	String taskCollaborator;
	boolean taskCollaboratorsWriteAuthorization;

	private RecordIdToCaptionConverter idToCaptionConverter = new RecordIdToCaptionConverter();

	public TaskCollaboratorItem(String taskCollaborator,
								Boolean collaboratorWriteAuthorization) {
		this.taskCollaborator = taskCollaborator;
		this.taskCollaboratorsWriteAuthorization = collaboratorWriteAuthorization;
	}

	public TaskCollaboratorItem() {
	}

	public String getTaskCollaborator() {
		return taskCollaborator;
	}

	public void setTaskCollaborator(String taskCollaborator) {
		this.taskCollaborator = taskCollaborator;
	}

	public boolean isTaskCollaboratorsWriteAuthorization() {
		return taskCollaboratorsWriteAuthorization;
	}

	public void setCollaboratorReadAuthorization(boolean collaboratorReadAuthorization) {
		this.taskCollaboratorsWriteAuthorization = collaboratorReadAuthorization;
	}
}
