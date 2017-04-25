package com.constellio.app.modules.rm.extensions.schema;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.TransactionExecutionBeforeSaveEvent;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.search.SearchServices;

import java.util.*;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMAvailableCapacityExtension extends RecordExtension {

	public static final String INSUFFICIENT_CONTAINER_RECORD_CAPACITY_ERROR = "insufficientContainerRecordCapacityError";
	public static final String INSUFFICIENT_STORAGE_SPACE_CAPACITY_ERROR = "insufficientStorageSpaceCapacityError";

	SearchServices searchServices;
	RMSchemasRecordsServices rm;
	AppLayerFactory appLayerFactory;

	public RMAvailableCapacityExtension(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void transactionExecutionBeforeSave(TransactionExecutionBeforeSaveEvent event) {
		if (event.isOnlySchemaType(ContainerRecord.SCHEMA_TYPE) && !event.isNewRecordImport()) {
			validateContainerRecord(event.getValidationErrors(), rm.wrapContainerRecords(event.getTransaction().getRecords()));
		}
	}

	@Override
	public void recordInCreationBeforeSave(RecordInCreationBeforeSaveEvent event) {
		if (event.isSingleRecordTransaction() && event.isSchemaType(StorageSpace.SCHEMA_TYPE) && !event.isNewRecordImport()) {
			validateStorageSpace(event.getValidationErrors(), rm.wrapStorageSpace(event.getRecord()));
		}
	}

	@Override
	public void recordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event) {
		if (event.isSingleRecordTransaction() && event.isSchemaType(StorageSpace.SCHEMA_TYPE)) {
			validateStorageSpace(event.getValidationErrors(), rm.wrapStorageSpace(event.getRecord()));
		}
	}

	private void validateContainerRecord(ValidationErrors errors, List<ContainerRecord> containerRecords) {

		KeyListMap<String, ContainerRecord> containerRecordsInMap = new KeyListMap<>();
		boolean containerMultipleValue = new RMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager()).isContainerMultipleValue();

		for (ContainerRecord containerRecord : containerRecords) {
			if (!containerMultipleValue && containerRecord.getStorageSpace() != null) {
				containerRecordsInMap.add(containerRecord.getStorageSpace(), containerRecord);
			}
		}

		for (Map.Entry<String, List<ContainerRecord>> entry : containerRecordsInMap.getMapEntries()) {
			StorageSpace storageSpace = rm.getStorageSpace(entry.getKey());
			validateContainerRecord(errors, storageSpace, entry.getValue());
		}
	}

	private void validateContainerRecord(ValidationErrors errors, StorageSpace storageSpace,
			List<ContainerRecord> containerRecords) {

		if (storageSpace.getCapacity() != null && storageSpace.getLinearSizeEntered() == null
				&& Toggle.STORAGE_SPACE_CAPACITIY_VALIDATION.isEnabled()) {
			long totalCapacity = 0;

			List<ContainerRecord> containerRecordsAtSameLevel = rm.searchContainerRecords(from(rm.containerRecord.schemaType())
					.where(rm.containerRecord.storageSpace()).isEqualTo(storageSpace.getId())
					.andWhere(rm.containerRecord.capacity()).isNotNull());

			Set<String> ids = new HashSet<>();
			for (ContainerRecord containerRecord : containerRecords) {
				ids.add(containerRecord.getId());
				if (containerRecord.getCapacity() != null) {
					totalCapacity += containerRecord.getCapacity().longValue();
				}
			}

			for (ContainerRecord record : containerRecordsAtSameLevel) {
				if (!ids.contains(record.getId())) {
					totalCapacity += record.getCapacity();
				}
			}

			if (totalCapacity > storageSpace.getCapacity()) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("storageSpace", storageSpace.getTitle());

				errors.add(RMAvailableCapacityExtension.class, INSUFFICIENT_CONTAINER_RECORD_CAPACITY_ERROR, parameters);
			}

		}

	}

	private void validateStorageSpace(ValidationErrors errors, StorageSpace storageSpace) {

		if (storageSpace.getParentStorageSpace() != null) {
			StorageSpace parentStorageSpace = rm.getStorageSpace(storageSpace.getParentStorageSpace());

			if (parentStorageSpace.getCapacity() != null && parentStorageSpace.getLinearSizeEntered() == null
					&& storageSpace.getCapacity() != null && Toggle.STORAGE_SPACE_CAPACITIY_VALIDATION.isEnabled()) {

				List<StorageSpace> storageSpacesAtSameLevel = rm.searchStorageSpaces(from(rm.storageSpace.schemaType())
						.where(rm.storageSpace.parentStorageSpace()).isEqualTo(parentStorageSpace.getId())
						.andWhere(rm.storageSpace.capacity()).isNotNull());

				long totalCapacity = storageSpace.getCapacity();
				for (StorageSpace record : storageSpacesAtSameLevel) {
					if (!record.getId().equals(storageSpace.getId())) {
						totalCapacity += record.getCapacity();
					}
				}

				if (totalCapacity > parentStorageSpace.getCapacity()) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("storageSpace", parentStorageSpace.getTitle());

					errors.add(RMAvailableCapacityExtension.class, INSUFFICIENT_STORAGE_SPACE_CAPACITY_ERROR, parameters);
				}

			}

		}
	}
}
