package com.constellio.app.modules.rm.pdfgenerator;

import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.Map;

public class PdfGeneratorMergeTaskParam implements AsyncTaskExecutionParams {
	private String collection;

	public PdfGeneratorMergeTaskParam(String collection) {
		this.collection = collection;
	}

	@Override
	public String getCollection() {
		return collection;
	}

	@Override
	public void logWarning(String code, Map<String, Object> parameters) {

	}

	@Override
	public void logError(String code, Map<String, Object> parameters) {

	}

	@Override
	public void incrementProgression(int numberToAdd) {

	}

	@Override
	public void resetProgression() {

	}

	@Override
	public void setProgressionUpperLimit(long progressionUpperLimit) {

	}

	@Override
	public AsyncTaskBatchProcess getBatchProcess() {
		return null;
	}

	@Override
	public ModelLayerFactory getModelLayerFactory() {
		//Should not be used...
		return null;
	}

	public void throwError(String code, Map<String, Object> parameters) throws ValidationException {
		ValidationErrors errors = new ValidationErrors();
		errors.add(PdfGeneratorAsyncTask.class, code, parameters);
		errors.throwIfNonEmpty();
	}
}

