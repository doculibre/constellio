package com.constellio.model.services.trash;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class TrashServices {
	private final ModelLayerFactory modelLayerFactory;
	private final String collection;
	private RecordServices recordServices;

	public TrashServices(ModelLayerFactory modelLayerFactory, String collection) {
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
	}

	public LogicalSearchQuery getTrashRecordsQueryForType(String selectedType, User currentUser) {
		MetadataSchemaType schema = modelLayerFactory
				.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(selectedType);
		LogicalSearchCondition condition = from(schema).where(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
		return new LogicalSearchQuery(condition).filteredWithUserDelete(currentUser).sortAsc(Schemas.TITLE);
	}

	public LogicalSearchQuery getTrashRecordsQueryForCollection(String collection, User currentUser) {
		LogicalSearchCondition condition = fromAllSchemasIn(collection).where(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
		return new LogicalSearchQuery(condition).filteredWithUserDelete(currentUser).sortDesc(Schemas.LOGICALLY_DELETED_ON);
	}

	public List<String> restoreSelection(Set<String> selectedRecords, User currentUser) {
		List<String> returnList = new ArrayList<>();
		for (String recordId : selectedRecords) {
			Record record = recordServices().getDocumentById(recordId);
			if (recordServices().isRestorable(record, currentUser)) {
				recordServices().restore(record, currentUser);
			} else {
				returnList.add(recordId);
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

	public void deleteSelection(Set<String> selectedRecords, User currentUser) {
		for (String recordId : selectedRecords) {
			Record record = recordServices().getDocumentById(recordId);
			handleRecordPhysicalDelete(record, currentUser);
		}
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

	public List<String> getRelatedRecords(String recordId) {
		//TODO
		return new ArrayList<>();
	}

	public LogicalSearchQuery getTrashRecordsQueryForCollectionDeletedBeforeDate(String collection, LocalDateTime deleteDate) {
		LogicalSearchCondition condition = fromAllSchemasIn(collection).where(Schemas.LOGICALLY_DELETED_STATUS).isTrue()
				.andWhere(Schemas.LOGICALLY_DELETED_ON).isLessOrEqualThan(deleteDate);
		return new LogicalSearchQuery(condition).sortDesc(Schemas.LOGICALLY_DELETED_ON);
	}

	public void handleRecordPhysicalDelete(Record recordToDelete, User currentUser) {
		//TODO
		recordServices().physicallyDeleteFromTrash(recordToDelete, currentUser);
	}
}
