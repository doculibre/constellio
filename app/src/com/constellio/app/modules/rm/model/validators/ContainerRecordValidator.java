package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.records.RecordValidatorParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerRecordValidator implements RecordValidator {

	public static final String CONTAINER_CAPACITY_MUST_BE_GREATER_OR_EQUAL_TO_LINEAR_SIZE = "containerCapacityMustBeGreaterOrEqualToLinearSize";
	public static final String STORAGE_SPACE_CANNOT_CONTAIN_THIS_TYPE_OF_CONTAINER = "storageSpaceCannotContainThisTypeOfContainer";
	public static final String CAPACITY = "capacity";
	public static final String LINEAR_SIZE = "linearSize";
	public static final String LINEAR_SIZE_ENTERED = "linearSizeEntered";
	public static final String LINEAR_SIZE_SUM = "linearSizeSum";
	public static final String STORAGE_SPACE = "storageSpace";

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

		if(!Boolean.TRUE.equals(params.getConfigProvider().get(RMConfigs.IS_CONTAINER_MULTIVALUE)) && container.getStorageSpace() != null) {
			StorageSpace storageSpace = new StorageSpace(params.getRecord(container.getStorageSpace()), params.getTypes());
			if(!canContain(storageSpace, container.getType(), params.getRecordProvider(), params.getTypes())) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put(STORAGE_SPACE, formatToParameter(storageSpace.getTitle()));

				params.getValidationErrors().add(ContainerRecordValidator.class, STORAGE_SPACE_CANNOT_CONTAIN_THIS_TYPE_OF_CONTAINER, parameters);
			}
		}
	}

	private String formatToParameter(Object parameter) {
		if(parameter == null) {
			return "";
		}
		return parameter.toString();
	}

	public boolean canContain(StorageSpace storageSpace, String containerRecordType, RecordProvider recordProvider, MetadataSchemaTypes types) {
		if(containerRecordType == null) {
			return true;
		}
		List<String> containerRecordTypeList = new ArrayList<>();
		StorageSpace currentStorage = storageSpace;
		while (currentStorage != null) {

			if(currentStorage.getContainerType() != null && !currentStorage.getContainerType().isEmpty()) {
				containerRecordTypeList = currentStorage.getContainerType();
				break;
			} else if(currentStorage.getParentStorageSpace() == null) {
				break;
			}
			currentStorage = new StorageSpace(recordProvider.getRecord(currentStorage.getParentStorageSpace()), types);
		}

		return (containerRecordTypeList == null || containerRecordTypeList.isEmpty()) ? true : containerRecordTypeList.contains(containerRecordType);
	}
}
