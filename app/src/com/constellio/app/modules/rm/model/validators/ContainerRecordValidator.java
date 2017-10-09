package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.records.RecordValidatorParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerRecordValidator implements RecordValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContainerRecordValidator.class);
	public static final String CONTAINER_CAPACITY_MUST_BE_GREATER_OR_EQUAL_TO_LINEAR_SIZE = "containerCapacityMustBeGreaterOrEqualToLinearSize";
	public static final String STORAGE_SPACE_CANNOT_CONTAIN_THIS_TYPE_OF_CONTAINER = "storageSpaceCannotContainThisTypeOfContainer";
	public static final String STORAGE_SPACE_CAN_CONTAIN_ONLY_ONE_CONTAINER = "storageSpaceCanContainOnlyOneContainer";
	public static final String FIRST_TRANSFER_REPORT_DATE_CANNOT_BE_EDITED = "firstTransferReportDateCannotBeEdited";
	public static final String FIRST_DEPOSIT_REPORT_DATE_CANNOT_BE_EDITED = "firstDepositReportDateCannotBeEdited";
	public static final String CAPACITY = "capacity";
	public static final String LINEAR_SIZE = "linearSize";
	public static final String LINEAR_SIZE_ENTERED = "linearSizeEntered";
	public static final String LINEAR_SIZE_SUM = "linearSizeSum";
	public static final String STORAGE_SPACE = "storageSpace";
	public static final String FIRST_TRANSFER_REPORT_DATE = "firstTransferReportDate";
	public static final String FIRST_DEPOSIT_REPORT_DATE = "firstDepositReportDate";
	public static final String PREFIX = "prefix";

	@Override
	public void validate(RecordValidatorParams params) {
		ContainerRecord container = new ContainerRecord(params.getValidatedRecord(), params.getTypes());
		validate(container, params);
	}

	private void validate(ContainerRecord container, RecordValidatorParams params) {
		Double capacity = container.getCapacity();
		Double linearSize = container.getLinearSize();

		List<String> storageSpaceIds = container.getStorageSpaceList();

		List<StorageSpace> storageSpaces = new ArrayList<>();
		for(String storageSpaceId: storageSpaceIds) {
			storageSpaces.add(new StorageSpace(params.getRecord(storageSpaceId), params.getTypes()));
		}

		if(capacity != null && linearSize != null && linearSize > capacity) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(CAPACITY, formatToParameter(capacity));
			parameters.put(LINEAR_SIZE, formatToParameter(linearSize));
			parameters.put(LINEAR_SIZE_ENTERED, formatToParameter(container.getLinearSizeEntered()));
			parameters.put(LINEAR_SIZE_SUM, formatToParameter(container.getLinearSizeSum()));
			parameters.put(PREFIX, formatToParameter(container.getTitle() + " (" + container.getId() + ")", " : "));

			params.getValidationErrors().add(ContainerRecordValidator.class, CONTAINER_CAPACITY_MUST_BE_GREATER_OR_EQUAL_TO_LINEAR_SIZE, parameters);
		}

		for(StorageSpace storageSpace: storageSpaces) {
			if(!canContain(storageSpace, container.getType(), params.getRecordProvider(), params.getTypes())) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put(STORAGE_SPACE, formatToParameter(storageSpace.getTitle()));
				parameters.put(PREFIX, formatToParameter(container.getTitle() + " (" + container.getId() + ")", " : "));

				params.getValidationErrors().add(ContainerRecordValidator.class, STORAGE_SPACE_CANNOT_CONTAIN_THIS_TYPE_OF_CONTAINER, parameters);
			}
		}

		if(container.getWrappedRecord().isSaved()) {
			Object originalFirstTransferReportDate = container.getOriginal(ContainerRecord.FIRST_TRANSFER_REPORT_DATE);
			if(originalFirstTransferReportDate != null && !originalFirstTransferReportDate.equals(container.getFirstTransferReportDate())) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put(FIRST_TRANSFER_REPORT_DATE, formatToParameter(container.getFirstTransferReportDate()));
				parameters.put(PREFIX, formatToParameter(container.getTitle() + " (" + container.getId() + ")", " : "));

				params.getValidationErrors().add(ContainerRecordValidator.class, FIRST_TRANSFER_REPORT_DATE_CANNOT_BE_EDITED, parameters);
			}

			Object originalFirstDepositReportDate = container.getOriginal(ContainerRecord.FIRST_DEPOSIT_REPORT_DATE);
			if(originalFirstDepositReportDate != null && !originalFirstDepositReportDate.equals(container.getFirstDepositReportDate())) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put(FIRST_DEPOSIT_REPORT_DATE, formatToParameter(container.getFirstDepositReportDate()));
				parameters.put(PREFIX, formatToParameter(container.getTitle() + " (" + container.getId() + ")", " : "));

				params.getValidationErrors().add(ContainerRecordValidator.class, FIRST_DEPOSIT_REPORT_DATE_CANNOT_BE_EDITED, parameters);
			}
		}

		if(Boolean.TRUE.equals(params.getConfigProvider().get(RMConfigs.IS_CONTAINER_MULTIVALUE))) {
			validateStorageSpaceIsOnlyContainingOneContainer(container, storageSpaces, params);
		}
	}

	private void validateStorageSpaceIsOnlyContainingOneContainer(ContainerRecord container, List<StorageSpace> storageSpaces, RecordValidatorParams params) {
		List<String> originalStorageSpaces = null;
		if(container.getWrappedRecord().isSaved()) {
			originalStorageSpaces = container.getOriginal(ContainerRecord.STORAGE_SPACE);
		}
		if(originalStorageSpaces == null) {
			originalStorageSpaces = new ArrayList<>();
		}
		for(StorageSpace storageSpace: storageSpaces) {
			Double numberOfContainers = storageSpace.getNumberOfContainers();
			if(container.getWrappedRecord().isSaved()) {
				if(!originalStorageSpaces.contains(storageSpace.getId())) {
					if(numberOfContainers != null && numberOfContainers > 0){
						Map<String, Object> parameters = new HashMap<>();
						parameters.put(STORAGE_SPACE, formatToParameter(storageSpace.getTitle()));
						parameters.put(PREFIX, formatToParameter(container.getTitle() + " (" + container.getId() + ")", " : "));

						params.getValidationErrors().add(ContainerRecordValidator.class, STORAGE_SPACE_CAN_CONTAIN_ONLY_ONE_CONTAINER, parameters);
					}
				}
			} else {
				if(numberOfContainers != null && numberOfContainers > 0){
					Map<String, Object> parameters = new HashMap<>();
					parameters.put(STORAGE_SPACE, formatToParameter(storageSpace.getTitle()));
					parameters.put(PREFIX, formatToParameter(container.getTitle() + " (" + container.getId() + ")", " : "));

					params.getValidationErrors().add(ContainerRecordValidator.class, STORAGE_SPACE_CAN_CONTAIN_ONLY_ONE_CONTAINER, parameters);
				}
			}
		}
	}

	private String formatToParameter(Object parameter) {
		if(parameter == null) {
			return "";
		}
		return parameter.toString();
	}

	private String formatToParameter(Object parameter, String suffix) {
		if(parameter == null) {
			return formatToParameter(parameter);
		} else {
			return formatToParameter(parameter) + suffix;
		}
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

	public static double round(double value, int decimals) {
		long factor = (long) Math.pow(10, decimals);
		value = value * factor;
		long longValue = Math.round(value);
		return (double) longValue / factor;
	}
}
