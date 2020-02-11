package com.constellio.model.services.records.reindexing;

import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ReindexingSchemaTypeRecordsProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReindexingSchemaTypeRecordsProvider.class);

	Set<Integer> ids = new HashSet<>();
	int iteration = 0;
	int mainThreadQueryRows;
	Set<Integer> skipped = new HashSet<>();
	Set<Integer> idsInCurrentBatch;

	RecordServices recordServices;
	SearchServices searchServices;
	Set<Integer> lastSkippedRecords = new HashSet<>();
	boolean requiringAnotherIteration;
	MetadataSchemaType type;
	int dependencyLevel;
	int thresholdForReturningLastIgnoredDocumentById;
	int handled;
	boolean selfParentReference;

	public ReindexingSchemaTypeRecordsProvider(ModelLayerFactory modelLayerFactory, int mainThreadQueryRows,
											   MetadataSchemaType type, int dependencyLevel,
											   int thresholdForReturningLastIgnoredDocumentById) {

		for (Metadata metadata : type.getAllMetadatas()) {
			selfParentReference |= (metadata.getType() == REFERENCE
									&& metadata.getReferencedSchemaTypeCode().equals(type.getCode()));
		}

		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.mainThreadQueryRows = mainThreadQueryRows;
		this.type = type;
		this.dependencyLevel = dependencyLevel;
		this.thresholdForReturningLastIgnoredDocumentById = thresholdForReturningLastIgnoredDocumentById;
	}

	public Iterator<Record> startNewSchemaTypeIteration() {
		iteration++;
		idsInCurrentBatch = new HashSet<>();

		final Iterator<Record> recordsIterator;
		if (!skipped.isEmpty() && skipped.size() < thresholdForReturningLastIgnoredDocumentById) {
			//Returning some documents by id may be faster than iterating over all records
			recordsIterator = newRecordsIteratorById(skipped);

		} else {
			LogicalSearchQuery query = new LogicalSearchQuery(from(type).returnAll());
			query.filteredByVisibilityStatus(VisibilityStatusFilter.ALL);

			if (dependencyLevel % 2 == 0) {
				recordsIterator = searchServices.recordsIterator(query, mainThreadQueryRows);
			} else {
				recordsIterator = searchServices.reverseRecordsIterator(query, mainThreadQueryRows);

			}

		}

		skipped.clear();
		return newRecordsIteratorAdaptorNotReturningAlreadyHandled(recordsIterator);
	}

	private Iterator<Record> newRecordsIteratorAdaptorNotReturningAlreadyHandled(
			final Iterator<Record> nestedRecordsIterator) {
		return new LazyIterator<Record>() {
			@Override
			protected Record getNextOrNull() {
				while (nestedRecordsIterator.hasNext()) {
					Record record = nestedRecordsIterator.next();
					int intId = record.getRecordId().intValue();
					if (!selfParentReference || (!ids.contains(intId) && !idsInCurrentBatch.contains(intId))) {

						return record;
					}

				}

				return null;
			}
		};
	}

	private Iterator<Record> newRecordsIteratorById(Set<Integer> intIdsSet) {
		List<Integer> ids = new ArrayList<>(intIdsSet);
		if (dependencyLevel % 2 == 0) {
			Collections.sort(ids);
		} else {
			Collections.sort(ids, (o1, o2) -> -1 * o1.compareTo(o2));
		}

		final Iterator<Integer> idsIterator = ids.iterator();

		return new LazyIterator<Record>() {
			@Override
			protected Record getNextOrNull() {
				while (idsIterator.hasNext()) {

					Integer intId = idsIterator.next();
					try {
						return recordServices.getDocumentById(RecordId.id(intId).stringValue());
					} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
						//Skipping this record
					}

				}
				return null;
			}
		};
	}

	int markRecordAsSkipped(String id) {
		int intId = RecordId.toId(id).intValue();
		skipped.add(intId);
		boolean alreadyHandledInPreviousIteration = ids.remove(intId);
		boolean alreadyHandledInSameIteration = idsInCurrentBatch.remove(intId);
		return skipped.size();
	}

	void markRecordAsHandled(Record record) {
		if (selfParentReference) {
			idsInCurrentBatch.add(record.getRecordId().intValue());
		}
	}

	void markIterationAsFinished() {
		if (selfParentReference) {
			ids.addAll(idsInCurrentBatch);
		}
		requiringAnotherIteration = !(skipped.size() == 0 || skipped.equals(lastSkippedRecords));
		lastSkippedRecords = new HashSet<>(skipped);
	}

	int getSkippedRecordsCount() {
		return skipped.size();
	}


	boolean isRequiringAnotherIteration() {
		return requiringAnotherIteration;
	}

	/**
	 * 1-based
	 *
	 * @return
	 */
	int getCurrentIteration() {
		return iteration;
	}

	public boolean isAlreadyHandledInCurrentOrPreviousBatch(String id) {
		int intId = RecordId.toId(id).intValue();
		return selfParentReference && (ids.contains(intId) || idsInCurrentBatch.contains(intId));
	}

	public boolean isAlreadyHandledInPreviousBatch(String id) {
		int intId = RecordId.toId(id).intValue();
		return selfParentReference && ids.contains(intId);
	}

	public int getMainThreadQueryRows() {
		return mainThreadQueryRows;
	}
}
