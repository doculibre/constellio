package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.wrappers.User;

public abstract class TaskManagementPresenterExtension {

	public abstract void afterCompletionActions(User currentUser);

	public abstract void beforeCompletionActions(Task currentUser);

}
