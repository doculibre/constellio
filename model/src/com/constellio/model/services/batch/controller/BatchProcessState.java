package com.constellio.model.services.batch.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class BatchProcessState implements Serializable {

	List<ValidationError> warnings = new ArrayList<>();
	List<ValidationError> errors = new ArrayList<>();
	long currentlyProcessed = 0;
	long totalToProcess = 0;

	public List<ValidationError> getWarnings() {
		return warnings;
	}

	public BatchProcessState setWarnings(List<ValidationError> warnings) {
		this.warnings = warnings;
		return this;
	}

	public List<ValidationError> getErrors() {
		return errors;
	}

	public BatchProcessState setErrors(List<ValidationError> errors) {
		this.errors = errors;
		return this;
	}

	public ValidationErrors getValidationErrors() {
		ValidationErrors validationErrors = new ValidationErrors();

		validationErrors.addAll(errors);
		validationErrors.addAllWarnings(warnings);

		return validationErrors;
	}

	public BatchProcessState setCurrentlyProcessed(long currentlyProcessed) {
		this.currentlyProcessed = currentlyProcessed;
		return this;
	}

	public BatchProcessState setTotalToProcess(long totalToProcess) {
		this.totalToProcess = totalToProcess;
		return this;
	}

	public long getCurrentlyProcessed() {
		return currentlyProcessed;
	}

	public long getTotalToProcess() {
		return totalToProcess;
	}

	public long incrementCurrentlyProcessed() {
		return ++this.currentlyProcessed;
	}

	public long incrementCurrentlyProcessed(int numberToIncrement) {
		return this.currentlyProcessed += numberToIncrement;
	}
}