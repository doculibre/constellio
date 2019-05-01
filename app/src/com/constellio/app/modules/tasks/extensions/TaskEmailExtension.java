package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.modules.tasks.model.wrappers.Task;

import java.util.List;

public interface TaskEmailExtension {
	List<String> newParameters(Task task);
}
