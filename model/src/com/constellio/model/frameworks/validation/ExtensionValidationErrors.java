package com.constellio.model.frameworks.validation;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;

public class ExtensionValidationErrors {
	private ValidationErrors validationErrors;
	private ExtensionBooleanResult extensionBooleanResult;

	public ExtensionValidationErrors(ValidationErrors validationErrors,
									 ExtensionBooleanResult extensionBooleanResult) {
		this.validationErrors = validationErrors;
		this.extensionBooleanResult = extensionBooleanResult;
	}

	public ExtensionValidationErrors(
			ExtensionBooleanResult extensionBooleanResult) {
		this(null, extensionBooleanResult);
	}

	public ValidationErrors getValidationErrors() {
		return validationErrors;
	}

	public ExtensionBooleanResult getExtensionBooleanResult() {
		return extensionBooleanResult;
	}
}
