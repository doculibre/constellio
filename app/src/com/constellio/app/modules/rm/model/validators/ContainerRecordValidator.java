package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordValidatorParams;

import java.util.HashMap;
import java.util.Map;

public class ContainerRecordValidator implements RecordValidator {

	public static final String CONTAINER_CAPACITY_MUST_BE_GREATER_OR_EQUAL_TO_LINEAR_SIZE = "containerCapacityMustBeGreaterOrEqualToLinearSize";
	public static final String CAPACITY = "capacity";
	public static final String LINEAR_SIZE = "linearSize";
	public static final String LINEAR_SIZE_ENTERED = "linearSizeEntered";
	public static final String LINEAR_SIZE_SUM = "linearSizeSum";

	@Override
	public void validate(RecordValidatorParams params) {
		ContainerRecord container = new ContainerRecord(params.getValidatedRecord(), params.getTypes());
		validate(container, params);
	}

	private void validate(ContainerRecord container, RecordValidatorParams params) {
		Double capacity = container.getCapacity();
		Double linearSize = container.getLinearSize();
		if(capacity != null && linearSize != null && linearSize > capacity) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(CAPACITY, formatToParameter(capacity));
			parameters.put(LINEAR_SIZE, formatToParameter(linearSize));
			parameters.put(LINEAR_SIZE_ENTERED, formatToParameter(container.getLinearSizeEntered()));
			parameters.put(LINEAR_SIZE_SUM, formatToParameter(container.getLinearSizeSum()));

			params.getValidationErrors().add(ContainerRecordValidator.class, CONTAINER_CAPACITY_MUST_BE_GREATER_OR_EQUAL_TO_LINEAR_SIZE, parameters);
		}
	}

	private String formatToParameter(Double parameter) {
		if(parameter == null) {
			return "";
		}
		return parameter.toString();
	}
}
