package com.constellio.model.services.records.reindexing;

import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ReindexingSchemaTypeRecordsProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReindexingSchemaTypeRecordsProvider.class);

	Set<String> ids = new HashSet<>();
	int iteration = 0;
	int mainThreadQueryRows;
	Set<String> skipped = new HashSet<>();
	Set<String> idsInCurrentBatch;

	RecordServices recordServices;
	SearchServices searchServices;
	Set<String> lastSkippedRecords = new HashSet<>();
	boolean requiringAnotherIteration;
	MetadataSchemaType type;
	int dependencyLevel;
	int thresholdForReturningLastIgnoredDocumentById;
	ReindexingRecordPriorityInfo info;

	public ReindexingSchemaTypeRecordsProvider(ModelLayerFactory modelLayerFactory, int mainThreadQueryRows,
											   MetadataSchemaType type, int dependencyLevel,
											   int thresholdForReturningLastIgnoredDocumentById,
											   ReindexingRecordPriorityInfo info) {
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.mainThreadQueryRows = mainThreadQueryRows;
		this.type = type;
		this.dependencyLevel = dependencyLevel;
		this.thresholdForReturningLastIgnoredDocumentById = thresholdForReturningLastIgnoredDocumentById;
		this.info = info;
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
		//final List<Metadata> metadatas = type.getAllMetadatas().onlyParentReferences().onlyReferencesToType(type.getCode());
		//		final Integer lastIterationOfADependencyLevelUnder =
		//				dependencyLevel == 0 ? null : info.getLastIterationOf(dependencyLevel - 1, type.getCode());
		//		final Integer correspondingLastIterationADependencyLevelUnder = lastIterationOfADependencyLevelUnder == null ? null :
		//				lastIterationOfADependencyLevelUnder - iteration;
		return new LazyIterator<Record>() {
			@Override
			protected Record getNextOrNull() {
				while (nestedRecordsIterator.hasNext()) {
					Record record = nestedRecordsIterator.next();
					if (!ids.contains(record.getId()) && !idsInCurrentBatch.contains(record.getId())) {

						//						if (correspondingLastIterationADependencyLevelUnder != null) {
						//
						//							Integer iterationADependencyLevelUnder = info
						//									.getIterationOf(dependencyLevel - 1, type.getCode(), record.getId());
						//
						//							if (iterationADependencyLevelUnder != null) {
						//								int correspondingIterationADependencyLevelUnder =
						//										lastIterationOfADependencyLevelUnder - iteration;
						//
						//								if (correspondingIterationADependencyLevelUnder)
						//							}
						//
						//						}

						info.markHasHandledAtIteration(dependencyLevel, type.getCode(), record.getId(), iteration);
						return record;
					}

				}

				return null;
			}
		};
	}

	private Iterator<Record> newRecordsIteratorById(Set<String> idsSet) {
		List<String> ids = new ArrayList<>(idsSet);
		if (dependencyLevel % 2 == 0) {
			Collections.sort(ids);
		} else {
			Collections.sort(ids, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return -1 * o1.compareTo(o2);
				}
			});
		}

		final Iterator<String> idsIterator = ids.iterator();

		return new LazyIterator<Record>() {
			@Override
			protected Record getNextOrNull() {
				while (idsIterator.hasNext()) {

					String id = idsIterator.next();
					try {
						Record record = recordServices.getDocumentById(id);
						info.markHasHandledAtIteration(dependencyLevel, type.getCode(), record.getId(), iteration);
						return record;
					} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
						//Skipping this record
					}

				}
				return null;
			}
		};
	}

	int markRecordAsSkipped(String id) {
		skipped.add(id);
		boolean alreadyHandledInPreviousIteration = ids.remove(id);
		boolean alreadyHandledInSameIteration = idsInCurrentBatch.remove(id);
		return skipped.size();
	}

	void markRecordAsHandled(Record record) {
		idsInCurrentBatch.add(record.getId());
	}

	void markIterationAsFinished() {
		ids.addAll(idsInCurrentBatch);
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
		return ids.contains(id) || idsInCurrentBatch.contains(id);
	}

	public boolean isAlreadyHandledInPreviousBatch(String id) {
		return ids.contains(id);
	}
}
