package com.constellio.app.modules.tasks.extensions;

import com.constellio.model.entities.records.wrappers.User;

public abstract class TaskManagementPresenterExtension {
	public abstract void automaticallyAssignAvailableTasks(User currentUser);
}
