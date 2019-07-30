package com.constellio.model.services.trash;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.events.schemas.SchemaEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;

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
		MetadataSchemaType schemaType = modelLayerFactory
				.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(selectedType);
		if (!schemaType.hasSecurity()) {
			LogicalSearchCondition deletableRecordsForUnsecuredType = getDeletableRecordsForUnsecuredType(selectedType, currentUser);
			if (deletableRecordsForUnsecuredType != null) {
				return new LogicalSearchQuery().setCondition(from(schemaType).whereAllConditions(deletableRecordsForUnsecuredType)).sortDesc(Schemas.LOGICALLY_DELETED_ON);
			} else {
				return new LogicalSearchQuery().setCondition(LogicalSearchQueryOperators.impossibleCondition(collection));
			}
		}
		MetadataSchemaType schema = modelLayerFactory
				.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(selectedType);
		LogicalSearchCondition condition = from(schema).where(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
		return new LogicalSearchQuery(condition).filteredWithUserDelete(currentUser).sortDesc(Schemas.LOGICALLY_DELETED_ON);
	}

	private LogicalSearchCondition getDeletableRecordsForUnsecuredType(final String selectedType,
																	   final User currentUser) {
		ModelLayerCollectionExtensions extension = modelLayerFactory.getExtensions()
				.forCollection(collection);
		final LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(asList(selectedType), collection).returnAll());
		return extension.getPhysicallyDeletableQueryForSchemaType(new SchemaEvent() {

			@Override
			public String getSchemaCode() {
				return selectedType + "_default";
			}

			@Override
			public User getUser() {
				return currentUser;
			}
		});
	}

	public LogicalSearchQuery getTrashRecordsQueryForCollection(String collection, User currentUser) {
		List<MetadataSchemaType> trashSchemaList = getTrashSchemaTypes(collection, currentUser);
		List<String> securedSchemaList = new ArrayList<>();
		List<LogicalSearchCondition> conditionList = new ArrayList<>();
		for (MetadataSchemaType schemaType : trashSchemaList) {
			if (schemaType.hasSecurity()) {
				securedSchemaList.add(schemaType.getCode() + "_");
			} else {
				LogicalSearchCondition deletableRecordsForUnsecuredType = getDeletableRecordsForUnsecuredType(schemaType.getCode(), currentUser);
				if (deletableRecordsForUnsecuredType != null) {
					conditionList.add(deletableRecordsForUnsecuredType);
				}
			}
		}
		LogicalSearchCondition securedSchemaTypesCondition = fromAllSchemasIn(collection).where(Schemas.SCHEMA).isStartingWithTextFromAny(securedSchemaList).andWhere(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
		conditionList.add(securedSchemaTypesCondition);

		LogicalSearchCondition condition = anyConditions(conditionList);
		return new LogicalSearchQuery(condition).filteredWithUserDelete(currentUser).sortDesc(Schemas.LOGICALLY_DELETED_ON);
	}

	private List<MetadataSchemaType> getTrashSchemaTypes(String collection, final User currentUser) {
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

				@Override
				public User getUser() {
					return currentUser;
				}
			};
			if (extension.isPutInTrashBeforePhysicalDelete(schemaEvent)) {
				returnList.add(schemaType);
			}
		}
		return returnList;
	}

	public Map<String, String> restoreSelection(Set<String> selectedRecords, User currentUser) {
		Map<String, String> returnMap = new HashMap<>();
		for (String recordId : selectedRecords) {
			Record record = recordServices().getDocumentById(recordId);
			if (recordServices().isRestorable(record, currentUser)) {
				recordServices().restore(record, currentUser);
			} else {
				returnMap.put(recordId, record.getTitle());
			}
		}
		return returnMap;
	}

	private RecordServices recordServices() {
		if (recordServices == null) {
			recordServices = modelLayerFactory.newRecordServices();
		}
		return recordServices;
	}

	public Map<String, String> deleteSelection(Set<String> selectedRecords, User currentUser) {
		Map<String, String> returnMap = new HashMap<>();
		for (String recordId : selectedRecords) {
			Record record;
			try {
				record = recordServices().getDocumentById(recordId);
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				// Already deleted, ignore
				continue;
			}
			try {
				boolean deleted = handleRecordPhysicalDelete(record, currentUser);
				if (!deleted) {
					returnMap.put(recordId, record.getTitle());
				}
			} catch (Throwable e) {
				LOGGER.warn("record not deleted correctly from trash");
				returnMap.put(recordId, record.getTitle());
			}
		}
		return returnMap;
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

	public LogicalSearchQuery getTrashRecordsQueryForCollectionDeletedBeforeDate(String collection,
																				 LocalDateTime deleteDate) {
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(getTrashSchemaTypes(collection, null))
				.where(Schemas.LOGICALLY_DELETED_STATUS).isTrue()
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
				recordServices().update(recordToDelete.set(Schemas.ERROR_ON_PHYSICAL_DELETION, true),
						RecordUpdateOptions.userModificationsSafeOptions().setValidationsEnabled(false));
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

		public RecordsIdsAndTitles(Set<String> recordsIds, Set<String> recordsTitles) {
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
