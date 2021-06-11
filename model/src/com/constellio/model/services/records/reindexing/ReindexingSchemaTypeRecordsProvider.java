package com.constellio.model.services.records.reindexing;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.records.reindexing.ReindexingServices.LevelReindexingContext;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.model.utils.RecordDependencyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.records.GetRecordOptions.RETURNING_SUMMARY;
import static com.constellio.model.services.records.cache.dataStore.StreamCacheOption.SORTED_BY_IDS;
import static com.constellio.model.services.records.reindexing.ReindexingSchemaTypeRecordsProvider.FirstRecordsHandlingStatus.CURRENTLY_HANDLING_THEM;
import static com.constellio.model.services.records.reindexing.ReindexingSchemaTypeRecordsProvider.FirstRecordsHandlingStatus.DONE;
import static com.constellio.model.services.records.reindexing.ReindexingSchemaTypeRecordsProvider.FirstRecordsHandlingStatus.NOT_HANDLED_YET;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.stream.Collectors.toList;

public class ReindexingSchemaTypeRecordsProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReindexingSchemaTypeRecordsProvider.class);
	public static final int POSTPONED_RECORDS_LIMIT = 1000;

	Set<Integer> ids = new HashSet<>();
	int iteration = 0;
	int mainThreadQueryRows;
	Set<Integer> skipped = new HashSet<>();
	Set<Integer> idsInCurrentBatch;

	RecordServices recordServices;
	SearchServices searchServices;


	/**
	 * Skipped vs postponed
	 * Skipped records will be handled later, during an other iteration
	 * Postponed records are handled in the next batch
	 */
	Set<Integer> lastSkippedRecords = new HashSet<>();
	KeyListMap<Integer, Record> postponedRecords = new KeyListMap<>();
	List<Record> postponedRecordsReadyForReindexing = new ArrayList<>();
	boolean requiringAnotherIteration;
	boolean useSingleThread;
	int thresholdForReturningLastIgnoredDocumentById;
	int handled;
	int postponedReady;
	int postponedBlocked;

	public List<Record> getPostponedRecordsReadyForReindexing() {
		if (postponedRecordsReadyForReindexing.isEmpty()) {
			return Collections.emptyList();
		} else {

			synchronized (this) {
				List<Record> returnedRecords = new ArrayList<>(postponedRecordsReadyForReindexing);
				postponedReady -= returnedRecords.size();
				postponedRecordsReadyForReindexing.clear();
				return returnedRecords;
			}

		}
	}

	public boolean hasPostponedRecords() {
		return !postponedRecords.getNestedMap().isEmpty() || !postponedRecordsReadyForReindexing.isEmpty();
	}


	enum FirstRecordsHandlingStatus {NOT_HANDLED_YET, CURRENTLY_HANDLING_THEM, DONE}

	;
	FirstRecordsHandlingStatus firstRecordsHandlingStatus = NOT_HANDLED_YET;
	MetadataSchemaType type;
	RecordsCaches caches;
	LevelReindexingContext context;
	List<Metadata> selfReferenceParentMetadatas;
	List<Metadata> selfReferenceMetadatas;

	public ReindexingSchemaTypeRecordsProvider(ModelLayerFactory modelLayerFactory, int mainThreadQueryRows,
											   LevelReindexingContext context,
											   MetadataSchemaType type,
											   int thresholdForReturningLastIgnoredDocumentById) {

		this.selfReferenceParentMetadatas = type.getAllMetadatas().onlyParentReferences().onlyReferencesToType(type.getCode());
		this.selfReferenceMetadatas = type.getAllMetadatas().onlyReferencesToType(type.getCode());
		this.type = type;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.mainThreadQueryRows = mainThreadQueryRows;
		this.context = context;
		this.caches = modelLayerFactory.getRecordsCaches();
		this.thresholdForReturningLastIgnoredDocumentById = thresholdForReturningLastIgnoredDocumentById;
	}

	public boolean isUseSingleThread() {
		return useSingleThread;
	}

	public Iterator<Record> startNewSchemaTypeIteration() {

		iteration++;
		idsInCurrentBatch = new HashSet<>();
		useSingleThread = false;

		if (context.level % 2 == 0 && !selfReferenceParentMetadatas.isEmpty() && caches.areSummaryCachesInitialized()
			&& type.getCacheType().hasPermanentCache() && firstRecordsHandlingStatus == NOT_HANDLED_YET) {
			LOGGER.info("Finding records to reindex first...");
			List<RecordId> recordIds = findIdsToReindexFirst();

			if (!recordIds.isEmpty()) {
				firstRecordsHandlingStatus = FirstRecordsHandlingStatus.CURRENTLY_HANDLING_THEM;
				LOGGER.info("Found " + recordIds.size() + " records to reindex first");
				useSingleThread = true;
				return recordIds.stream().map(id -> recordServices.get(id)).iterator();

			} else {
				firstRecordsHandlingStatus = FirstRecordsHandlingStatus.DONE;
			}

		}

		final Iterator<Record> recordsIterator;
		if (!skipped.isEmpty() && skipped.size() < thresholdForReturningLastIgnoredDocumentById) {
			//Returning some documents by id may be faster than iterating over all records
			recordsIterator = newRecordsIteratorById(skipped);

		} else {
			LogicalSearchQuery query;
			if (context.params.getLimitToHierarchyOf() != null && !context.params.getLimitToHierarchyOf().isEmpty()) {
				query = new LogicalSearchQuery(from(type).where(Schemas.PATH_PARTS)
						.isIn(context.params.getLimitToHierarchyOf().stream().map(RecordId::stringValue).collect(toList())));
			} else {
				query = new LogicalSearchQuery(from(type).returnAll());
			}
			query.filteredByVisibilityStatus(VisibilityStatusFilter.ALL);

			query.setQueryExecutionMethod(QueryExecutionMethod.USE_SOLR);

			if (context.level % 2 == 0) {
				recordsIterator = searchServices.recordsIterator(query, mainThreadQueryRows);

			} else {
				recordsIterator = searchServices.reverseRecordsIterator(query, mainThreadQueryRows);

			}

		}

		skipped.clear();
		return newRecordsIteratorAdaptorNotReturningAlreadyHandled(recordsIterator);
	}

	private List<RecordId> findIdsToReindexFirst() {
		return RecordDependencyUtils.findFirstRecordsForIdSortedIteration(
				caches.stream(type, SORTED_BY_IDS),
				(record -> {
					for (Metadata metadata : selfReferenceParentMetadatas) {
						String parentId = record.get(metadata);
						if (StringUtils.isNotBlank(parentId)) {
							Record parent = recordServices.get(parentId, RETURNING_SUMMARY);
							return Collections.singletonList(parent);
						}
					}
					return Collections.emptyList();
				})
		);
	}

	private Iterator<Record> newRecordsIteratorAdaptorNotReturningAlreadyHandled(
			final Iterator<Record> nestedRecordsIterator) {
		return new LazyIterator<Record>() {
			@Override
			protected Record getNextOrNull() {
				while (nestedRecordsIterator.hasNext()) {
					Record record = nestedRecordsIterator.next();
					if (selfReferenceParentMetadatas.isEmpty()) {
						return record;
					}

					//We don't want to retrieve a intId for all records, since records, like events or saved search have UUIDs
					int intId = record.getRecordId().intValue();
					if (!ids.contains(intId) && !idsInCurrentBatch.contains(intId)) {

						return record;
					}

				}

				return null;
			}
		};
	}

	private Iterator<Record> newRecordsIteratorById(Set<Integer> intIdsSet) {
		List<Integer> ids = new ArrayList<>(intIdsSet);
		if (context.level % 2 == 0) {
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

	void markRecordAsHandledSoon(Record record) {
		if (!selfReferenceMetadatas.isEmpty()) {
			idsInCurrentBatch.add(record.getRecordId().intValue());
		}
	}

	void markTransactionRecordAsHandled(Transaction tx) {
		if (!selfReferenceMetadatas.isEmpty()) {
			synchronized (this) {
				for (Record record : tx.getRecords()) {
					int intValue = record.getRecordId().intValue();
					idsInCurrentBatch.remove(intValue);
					ids.add(intValue);

					if (postponedRecords.contains(intValue)) {
						List<Record> recordsReady = postponedRecords.get(intValue);
						postponedRecords.remove(intValue);
						postponedRecordsReadyForReindexing.addAll(recordsReady);
						postponedBlocked -= recordsReady.size();
						postponedReady += recordsReady.size();
						LOGGER.info(recordsReady.size() + " previously postponed records ready for  reindexing : " +
									recordsReady + "' [Blocked:" + postponedBlocked + "; Ready:" + postponedReady + "]");
					}
				}

			}
		}
	}


	synchronized boolean tryToPostpone(Record record, RecordId dependency) {
		LOGGER.info("Record '" + record.getId() + "' is postponed, waiting for '" + dependency.get().stringValue() + "' [Blocked:" + postponedBlocked + "; Ready:" + postponedReady + "]");
		if (idsInCurrentBatch.contains(dependency.intValue())) {
			if (postponedBlocked >= POSTPONED_RECORDS_LIMIT) {
				return false;
			}
			postponedBlocked++;
			postponedRecords.add(dependency.intValue(), record);
			return true;
		} else {
			//Records was saved at the same moment.. It is already ok..
			postponedReady++;
			postponedRecordsReadyForReindexing.add(record);
			return true;
		}
	}

	void markIterationAsFinished() {
		if (!selfReferenceMetadatas.isEmpty()) {
			ids.addAll(idsInCurrentBatch);
		}
		requiringAnotherIteration = !(skipped.size() == 0 || skipped.equals(lastSkippedRecords));

		if (firstRecordsHandlingStatus == CURRENTLY_HANDLING_THEM) {
			requiringAnotherIteration = true;
			firstRecordsHandlingStatus = DONE;
		}

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


	enum RecordReindexingStatus {IN_PREVIOUS_BATCH, IN_CURRENT_BATCH, LATER}

	public RecordReindexingStatus getRecordStatus(RecordId id) {

		if (selfReferenceMetadatas.isEmpty()) {
			throw new IllegalStateException("Records without same type parent reference are not monitored : " + type.getCode());
		}

		int intId = id.intValue();
		if (ids.contains(intId)) {
			return RecordReindexingStatus.IN_PREVIOUS_BATCH;
		}

		if (idsInCurrentBatch.contains(intId)) {
			return RecordReindexingStatus.IN_CURRENT_BATCH;
		}

		return RecordReindexingStatus.LATER;
	}

	//	public boolean isAlreadyHandledInCurrentOrPreviousBatch(String id) {
	//		int intId = RecordId.toId(id).intValue();
	//		return !selfReferenceParentMetadatas.isEmpty() && (ids.contains(intId) || idsInCurrentBatch.contains(intId));
	//	}
	//
	//	public boolean isAlreadyHandledInPreviousBatch(String id) {
	//		int intId = RecordId.toId(id).intValue();
	//		return !selfReferenceParentMetadatas.isEmpty() && ids.contains(intId);
	//	}

	public int getMainThreadQueryRows() {
		return mainThreadQueryRows;
	}
}
