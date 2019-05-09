package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.modules.tasks.extensions.param.PromptUserParam;
import com.constellio.model.frameworks.validation.ValidationException;

public class TaskPreCompletionExtention {
	public boolean isPromptUser(PromptUserParam completionExtention) throws ValidationException {
		return false;
	}
}
