package com.constellio.app.modules.tasks;

import com.constellio.model.entities.Permissions;

public class TasksPermissionsTo {
	public static Permissions PERMISSIONS = new Permissions(TaskModule.ID);

	private static String permission(String group, String permission) {
		return PERMISSIONS.add(group, permission);
	}

	// --------------------------------------------
	// Register groups and permissions below

	// Workflows
	public static final String WORKFLOWS_GROUP = "workflows";

	public static final String MANAGE_WORKFLOWS = permission(WORKFLOWS_GROUP, "manageWorkflows");
	public static final String START_WORKFLOWS = permission(WORKFLOWS_GROUP, "startWorkflows");
	public static final String DELETE_WORKFLOWS = permission(WORKFLOWS_GROUP, "deleteWorkflows");
	public static final String READ_WORKFLOW_EXECUTION = permission(WORKFLOWS_GROUP, "readWorkflowExecution");
	public static final String MODIFY_WORKFLOW_EXECUTION = permission(WORKFLOWS_GROUP, "modifyWorkflowExecution");
}
