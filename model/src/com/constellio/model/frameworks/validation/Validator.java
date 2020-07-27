package com.constellio.model.frameworks.validation;

public interface Validator<T> {

	public static final String RECORD = "record";
	public static final String METADATA_CODE = "metadataCode";
	public static final String METADATA_LABEL = "metadataLabel";

	public boolean isEssential();

	public void validate(T object, ValidationErrors validationErrors);

}
