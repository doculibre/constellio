package com.constellio.model.entities.batchprocess;

import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.utils.Parametrized;

public interface AsyncTask extends Parametrized {

	void execute(AsyncTaskExecutionParams params) throws ValidationException, Exception;
}
