package com.constellio.model.services.trash;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.events.schemas.SchemaEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class TrashServices {
	private static final Logger LOGGER = LoggerFactory.getLogger(TrashServices.class);
	private final ModelLayerFactory modelLayerFactory;
	private final String collection;
	private RecordServices recordServices;
	private final SearchServices searchServices;

	public TrashServices(ModelLayerFactory modelLayerFactory, String collection) {
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
		searchServices = modelLayerFactory.newSearchServices();
	}

	public LogicalSearchQuery getTrashRecordsQueryForType(String selectedType, User currentUser) {
		MetadataSchemaType schema = modelLayerFactory
				.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(selectedType);
		LogicalSearchCondition condition = from(schema).where(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
		return new LogicalSearchQuery(condition).filteredWithUserDelete(currentUser).sortAsc(Schemas.TITLE);
	}

	public LogicalSearchQuery getTrashRecordsQueryForCollection(String collection, User currentUser) {
		List<MetadataSchemaType> trashSchemaList = getTrashSchemaTypes(collection);
		LogicalSearchCondition condition = from(trashSchemaList).where(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
		return new LogicalSearchQuery(condition).filteredWithUserDelete(currentUser).sortDesc(Schemas.LOGICALLY_DELETED_ON);
	}

	private List<MetadataSchemaType> getTrashSchemaTypes(String collection) {
		List<MetadataSchemaType> returnList = new ArrayList<>();
		ModelLayerCollectionExtensions extension = modelLayerFactory.getExtensions()
				.forCollection(collection);
		List<MetadataSchemaType> allCollectionSchemaTypes = modelLayerFactory.getMetadataSchemasManager()
				.getSchemaTypes(collection).getSchemaTypes();
		for (MetadataSchemaType schemaType : allCollectionSchemaTypes) {
			final String schemaTypeCode = schemaType.getCode();
			SchemaEvent schemaEvent = new SchemaEvent() {
				@Override
				public String getSchemaCode() {
					return schemaTypeCode;
				}
			};
			if (extension.isPutInTrashBeforePhysicalDelete(schemaEvent)) {
				returnList.add(schemaType);
			}
		}
		return returnList;
	}

	public List<String> restoreSelection(Set<String> selectedRecords, User currentUser) {
		List<String> returnList = new ArrayList<>();
		for (String recordId : selectedRecords) {
			Record record = recordServices().getDocumentById(recordId);
			if (recordServices().isRestorable(record, currentUser)) {
				recordServices().restore(record, currentUser);
			} else {
				returnList.add(record.getTitle());
			}
		}
		return returnList;
	}

	private RecordServices recordServices() {
		if (recordServices == null) {
			recordServices = modelLayerFactory.newRecordServices();
		}
		return recordServices;
	}

	public Set<String> deleteSelection(Set<String> selectedRecords, User currentUser) {
		Set<String> returnSet = new HashSet<>();
		for (String recordId : selectedRecords) {
			Record record = recordServices().getDocumentById(recordId);
			try {
				boolean deleted = handleRecordPhysicalDelete(record, currentUser);
				if (!deleted) {
					returnSet.add(record.getTitle());
				}
			} catch (Throwable e) {
				LOGGER.warn("record not deleted correctly from trash");
				returnSet.add(recordId);
			}
		}
		return returnSet;
	}

	public Set<String> getTypesWithLogicallyDeletedRecords(String collection, User currentUser) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		LogicalSearchQuery query = getTrashRecordsQueryForCollection(collection, currentUser);
		query = query.addFieldFacet("schema_s").setNumberOfRows(0);

		SPEQueryResponse response = searchServices.query(query);
		List<String> schemasCodes = response.getFieldFacetValuesWithResults("schema_s");
		Set<String> returnSet = new HashSet<>();
		for (String schemaCode : schemasCodes) {
			String schemaType = StringUtils.substringBefore(schemaCode, "_");
			returnSet.add(schemaType);
		}
		return returnSet;
	}

	public RecordsIdsAndTitles getRelatedRecords(String recordId, User user) {
		Record record = recordServices().getDocumentById(recordId, user);
		try {
			recordServices().physicallyDelete(record, user, new RecordPhysicalDeleteOptions().setMostReferencesToNull(true));
			return new RecordsIdsAndTitles();
		} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords e) {
			return new RecordsIdsAndTitles(e.getRecordsIdsWithUnremovableReferences(),
					e.getRecordsTiltlesWithUnremovableReferences());
		}
	}

	public LogicalSearchQuery getTrashRecordsQueryForCollectionDeletedBeforeDate(String collection, LocalDateTime deleteDate) {
		LogicalSearchCondition condition = fromAllSchemasIn(collection).where(Schemas.LOGICALLY_DELETED_STATUS).isTrue()
				.andWhere(Schemas.LOGICALLY_DELETED_ON).isLessOrEqualThan(deleteDate);
		return new LogicalSearchQuery(condition).sortDesc(Schemas.LOGICALLY_DELETED_ON);
	}

	public boolean handleRecordPhysicalDelete(Record recordToDelete, User currentUser) {
		try {
			recordServices().physicallyDelete(recordToDelete, currentUser,
					new RecordPhysicalDeleteOptions().setMostReferencesToNull(true));
			return true;

		} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords e) {
			try {
				recordServices().add(recordToDelete.set(Schemas.ERROR_ON_PHYSICAL_DELETION, true));
			} catch (RecordServicesException e2) {
				throw new RuntimeException(e2);
			}
			return false;

		} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord e) {
			e.printStackTrace();
			return false;
		}
	}

	public long getLogicallyDeletedRecordsCount(String collection, User currentUser) {
		return searchServices.getResultsCount(getTrashRecordsQueryForCollection(collection, currentUser));
	}

	public static class RecordsIdsAndTitles {
		final Set<String> recordsIds;
		final Set<String> recordsTitles;
		public RecordsIdsAndTitles(Set<String> recordsIds,
				Set<String> recordsTitles) {
			this.recordsIds = recordsIds;
			this.recordsTitles = recordsTitles;
		}

		public RecordsIdsAndTitles() {
			this.recordsTitles = new HashSet<>();
			this.recordsIds = new HashSet<>();
		}

		public Set<String> getRecordsIds() {
			return recordsIds;
		}

		public Set<String> getRecordsTitles() {
			return recordsTitles;
		}
	}
}
