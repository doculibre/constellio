package com.constellio.model.services.records.reindexing;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ReindexingSchemaTypeRecordsProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReindexingSchemaTypeRecordsProvider.class);

	Set<String> ids = new HashSet<>();
	int iteration = 0;
	int mainThreadQueryRows;
	Set<String> skipped = new HashSet<>();
	Set<String> idsInCurrentBatch;

	SearchServices searchServices;
	int lastSkippedRecords = -1;
	boolean requiringAnotherIteration;
	MetadataSchemaType type;
	int dependencyLevel;

	public ReindexingSchemaTypeRecordsProvider(ModelLayerFactory modelLayerFactory, int mainThreadQueryRows,
			MetadataSchemaType type, int dependencyLevel) {
		this.searchServices = modelLayerFactory.newSearchServices();
		this.mainThreadQueryRows = mainThreadQueryRows;
		this.type = type;
		this.dependencyLevel = dependencyLevel;
	}

	Iterator<Record> startNewSchemaTypeIteration() {
		iteration++;
		idsInCurrentBatch = new HashSet<>();
		LogicalSearchQuery query = new LogicalSearchQuery().sortAsc(Schemas.IDENTIFIER);
		if (!skipped.isEmpty() && skipped.size() < 1000) {
			query.setCondition(from(type).where(IDENTIFIER).isIn(new ArrayList<>(skipped)));
		} else {
			query.setCondition(from(type).returnAll());
		}
		final Iterator<Record> recordsIterator = searchServices.recordsIterator(query, mainThreadQueryRows);

		final List<Metadata> metadatas = type.getAllMetadatas().onlyParentReferences().onlyReferencesToType(type.getCode());
		skipped.clear();
		return new LazyIterator<Record>() {
			@Override
			protected Record getNextOrNull() {
				while (recordsIterator.hasNext()) {
					Record record = recordsIterator.next();

					if (metadatas.isEmpty() || (!ids.contains(record.getId()) && !idsInCurrentBatch.contains(record.getId()))) {
						return record;
					}

				}

				return null;
			}
		};
	}

	void markRecordAsSkipped(Record record) {
		System.out.println("skipped:" + record.getId());
		skipped.add(record.getId());
		if (skipped.size() % 100 == 0) {
			LOGGER.info("Collection '" + record.getCollection() + "' - Indexing '" + record.getTypeCode() + "' : "
					+ skipped.size() + " records skipped");
		}
	}

	void markRecordAsHandled(Record record) {
		idsInCurrentBatch.add(record.getId());
	}

	void markIterationAsFinished() {
		ids.addAll(idsInCurrentBatch);
		requiringAnotherIteration = !(skipped.size() == 0 || skipped.size() == lastSkippedRecords);
		lastSkippedRecords = skipped.size();
	}

	int getSkippedRecordsCount() {
		return skipped.size();
	}

	boolean isRequiringAnotherIteration() {
		return requiringAnotherIteration;
	}

	/**
	 * 1-based
	 * @return
	 */
	int getCurrentIteration() {
		return iteration;
	}

	public boolean isAlreadyHandled(String id) {
		return ids.contains(id);
	}
}
