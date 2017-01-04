package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordValidatorParams;

import java.util.HashMap;
import java.util.Map;

public class StorageSpaceValidator implements RecordValidator {

	public static final String CHILD_CAPACITY_MUST_BE_LESSER_OR_EQUAL_TO_PARENT_CAPACITY = "childCapacityMustBeLesserOrEqualToParentCapacity";
	public static final String CAPACITY = "capacity";
	public static final String PARENT_CAPACITY = "parentCapacity";

	@Override
	public void validate(RecordValidatorParams params) {
		StorageSpace storageSpace = new StorageSpace(params.getValidatedRecord(), params.getTypes());
		validate(storageSpace, params);
	}

	private void validate(StorageSpace storageSpace, RecordValidatorParams params) {
		if(storageSpace.getParentStorageSpace() == null) {
			return;
		}
		Long capacity = storageSpace.getCapacity();
		StorageSpace parentStorageSpace = new StorageSpace(params.getRecordProvider().getRecord(storageSpace.getParentStorageSpace()), params.getTypes());
		Long parentCapacity = parentStorageSpace.getCapacity();
		if(capacity != null && parentCapacity != null && capacity > parentCapacity) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(CAPACITY, formatToParameter(capacity));
			parameters.put(PARENT_CAPACITY, formatToParameter(parentCapacity));

			params.getValidationErrors().add(StorageSpaceValidator.class, CHILD_CAPACITY_MUST_BE_LESSER_OR_EQUAL_TO_PARENT_CAPACITY, parameters);
		}
	}

	private String formatToParameter(Long parameter) {
		if(parameter == null) {
			return "";
		}
		return parameter.toString();
	}
}
