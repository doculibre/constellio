package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordValidatorParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageSpaceValidator implements RecordValidator {

	public static final String CHILD_CAPACITY_MUST_BE_LESSER_OR_EQUAL_TO_PARENT_CAPACITY = "childCapacityMustBeLesserOrEqualToParentCapacity";
	public static final String CONTAINER_TYPE_MUST_BE_INCLUDED_IN_PARENT_STORAGE_SPACE = "containerTypeMustBeIncludedInParentStorageSpace";
	public static final String CAPACITY = "capacity";
	public static final String PARENT_CAPACITY = "parentCapacity";
	public static final String CONTAINER_TYPE = "containerType";
	public static final String PARENT_CONTAINER_TYPE = "parentContainerType";

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

		List<ContainerRecordType> containerRecordTypeList = storageSpace.getContainerType();
		List<ContainerRecordType> parentContainerRecordTypeList = parentStorageSpace.getContainerType();
		if(parentContainerRecordTypeList != null && !parentContainerRecordTypeList.isEmpty() &&
				(containerRecordTypeList == null || containerRecordTypeList.isEmpty() || !parentContainerRecordTypeList.containsAll(containerRecordTypeList))) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(CONTAINER_TYPE, formatToParameter(containerRecordTypeList));
			parameters.put(PARENT_CAPACITY, formatToParameter(parentContainerRecordTypeList));

			params.getValidationErrors().add(StorageSpaceValidator.class, CONTAINER_TYPE_MUST_BE_INCLUDED_IN_PARENT_STORAGE_SPACE, parameters);
		}
	}

	private String formatToParameter(Long parameter) {
		if(parameter == null) {
			return "";
		}
		return parameter.toString();
	}

	private String formatToParameter(List<ContainerRecordType> parameter) {
		if(parameter == null || parameter.isEmpty()) {
			return "";
		}
		return parameter.toString();
	}
}
