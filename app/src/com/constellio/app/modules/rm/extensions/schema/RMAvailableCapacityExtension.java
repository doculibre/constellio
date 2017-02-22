package com.constellio.app.modules.rm.extensions.schema;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.search.SearchServices;

public class RMAvailableCapacityExtension extends RecordExtension {

	public static final String INSUFFICIENT_CONTAINER_RECORD_CAPACITY_ERROR = "insufficientContainerRecordCapacityError";
	public static final String INSUFFICIENT_STORAGE_SPACE_CAPACITY_ERROR = "insufficientStorageSpaceCapacityError";

	SearchServices searchServices;
	RMSchemasRecordsServices rm;

	public RMAvailableCapacityExtension(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	@Override
	public void recordCreated(RecordCreationEvent event) {

	}

	@Override
	public void recordModified(RecordModificationEvent event) {

	}

	@Override
	public void recordInCreationBeforeSave(RecordInCreationBeforeSaveEvent event) {
		if (event.isSingleRecordTransaction() && event.isSchemaType(StorageSpace.SCHEMA_TYPE)) {
			validateStorageSpace(event.getValidationErrors(), rm.wrapStorageSpace(event.getRecord()));
		}
		if (event.isSingleRecordTransaction() && event.isSchemaType(ContainerRecord.SCHEMA_TYPE)) {
			validateContainerRecord(event.getValidationErrors(), rm.wrapContainerRecord(event.getRecord()));
		}
	}

	@Override
	public void recordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event) {
		if (event.isSingleRecordTransaction() && event.isSchemaType(StorageSpace.SCHEMA_TYPE)) {
			validateStorageSpace(event.getValidationErrors(), rm.wrapStorageSpace(event.getRecord()));
		}
		if (event.isSingleRecordTransaction() && event.isSchemaType(ContainerRecord.SCHEMA_TYPE)) {
			validateContainerRecord(event.getValidationErrors(), rm.wrapContainerRecord(event.getRecord()));
		}
	}

	private void validateContainerRecord(ValidationErrors errors, ContainerRecord containerRecord) {

		if (containerRecord.getStorageSpace() != null) {
			StorageSpace storageSpace = rm.getStorageSpace(containerRecord.getStorageSpace());

			List<ContainerRecord> containerRecordsAtSameLevel = rm.searchContainerRecords(from(rm.containerRecord.schemaType())
					.where(rm.containerRecord.storageSpace()).isEqualTo(storageSpace.getId())
					.andWhere(rm.containerRecord.capacity()).isNotNull());

			if (storageSpace.getCapacity() != null && storageSpace.getLinearSizeEntered() == null
					&& containerRecord.getCapacity() != null) {
				long totalCapacity = containerRecord.getCapacity().longValue();
				for (ContainerRecord record : containerRecordsAtSameLevel) {
					if (!record.getId().equals(containerRecord.getId())) {
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
	}

	private void validateStorageSpace(ValidationErrors errors, StorageSpace storageSpace) {

		if (storageSpace.getParentStorageSpace() != null) {
			StorageSpace parentStorageSpace = rm.getStorageSpace(storageSpace.getParentStorageSpace());

			List<StorageSpace> storageSpacesAtSameLevel = rm.searchStorageSpaces(from(rm.storageSpace.schemaType())
					.where(rm.storageSpace.parentStorageSpace()).isEqualTo(parentStorageSpace.getId())
					.andWhere(rm.storageSpace.capacity()).isNotNull());

			if (parentStorageSpace.getCapacity() != null && parentStorageSpace.getLinearSizeEntered() == null
					&& storageSpace.getCapacity() != null) {
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
