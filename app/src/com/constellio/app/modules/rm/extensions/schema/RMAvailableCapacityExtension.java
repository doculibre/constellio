package com.constellio.app.modules.rm.extensions.schema;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.List;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.search.SearchServices;

public class RMAvailableCapacityExtension extends RecordExtension {

	public static final String INSUFFICIENT_CAPACITY_ERROR = "insufficientCapacityError";

	SearchServices searchServices;
	RMSchemasRecordsServices rm;

	public RMAvailableCapacityExtension(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	@Override
	public void recordCreated(RecordCreationEvent event) {
//		if (event.isSingleRecordTransaction() && event.isSchemaType(StorageSpace.SCHEMA_TYPE)) {
//			validateStorageSpace(event.getValidationErrors(), rm.wrapStorageSpace(event.getRecord()));
//		}
//		if (event.isSingleRecordTransaction() && event.isSchemaType(ContainerRecord.SCHEMA_TYPE)) {
//			validateContainerRecord(event.getValidationErrors(), rm.wrapContainerRecord(event.getRecord()));
//		}
	}

	@Override
	public void recordModified(RecordModificationEvent event) {
//		if (event.isSingleRecordTransaction() && event.isSchemaType(StorageSpace.SCHEMA_TYPE)) {
		//			validateStorageSpace(event.getValidationErrors(), rm.wrapStorageSpace(event.getRecord()));
		//		}
		//		if (event.isSingleRecordTransaction() && event.isSchemaType(ContainerRecord.SCHEMA_TYPE)) {
		//			validateContainerRecord(event.getValidationErrors(), rm.wrapContainerRecord(event.getRecord()));
		//		}
	}

	private void validateContainerRecord(ValidationErrors errors, ContainerRecord containerRecord) {

		if (containerRecord.getStorageSpace() != null) {
			StorageSpace storageSpace = rm.getStorageSpace(containerRecord.getStorageSpace());

			List<ContainerRecord> containerRecordsAtSameLevel = rm.searchContainerRecords(from(rm.containerRecord.schemaType())
					.where(rm.containerRecord.storageSpace()).isEqualTo(storageSpace.getId())
					.andWhere(rm.containerRecord.capacity()).isNotNull());

			if (storageSpace.getCapacity() != null) {
				long totalCapacity = containerRecord.getCapacity().longValue();
				for (ContainerRecord record : containerRecordsAtSameLevel) {
					if (!record.getId().equals(containerRecord.getId())) {
						totalCapacity += record.getCapacity();
					}
				}

				if (totalCapacity < storageSpace.getCapacity()) {
					errors.add(RMAvailableCapacityExtension.class, INSUFFICIENT_CAPACITY_ERROR);
				}

			}

		}
	}

	private void validateStorageSpace(ValidationErrors errors, StorageSpace storageSpace) {

		//		if (containerRecord.getStorageSpace() != null) {
		//			StorageSpace storageSpace = rm.getStorageSpace(containerRecord.getStorageSpace());
		//
		//			List<ContainerRecord> containerRecordsAtSameLevel = rm.searchContainerRecords(from(rm.containerRecord.schemaType())
		//					.where(rm.containerRecord.storageSpace()).isEqualTo(storageSpace.getId())
		//					.andWhere(rm.containerRecord.capacity()).isNotNull());
		//
		//			if (storageSpace.getCapacity() != null) {
		//				long totalCapacity = containerRecord.getCapacity().longValue();
		//				for (ContainerRecord record : containerRecordsAtSameLevel) {
		//					if (!record.getId().equals(containerRecord.getId())) {
		//						totalCapacity += record.getCapacity();
		//					}
		//				}
		//
		//				if (totalCapacity < storageSpace.getCapacity()) {
		//					errors.add(RMAvailableCapacityExtension.class, INSUFFICIENT_CAPACITY_ERROR);
		//				}
		//
		//			}
		//
		//		}
	}
}
