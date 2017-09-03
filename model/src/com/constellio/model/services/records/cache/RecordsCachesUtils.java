package com.constellio.model.services.records.cache;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class RecordsCachesUtils {

	public static CacheInsertionStatus evaluateCacheInsert(Record insertedRecord) {

		if (insertedRecord == null) {
			return CacheInsertionStatus.REFUSED_NULL;
		}

		if (insertedRecord.isDirty()) {
			return CacheInsertionStatus.REFUSED_DIRTY;
		}

		if (!insertedRecord.isSaved()) {
			return CacheInsertionStatus.REFUSED_UNSAVED;
		}

		if (!insertedRecord.isFullyLoaded()) {
			return CacheInsertionStatus.REFUSED_NOT_FULLY_LOADED;
		}

		return CacheInsertionStatus.ACCEPTED;
	}

	public static boolean hasNoUnsupportedFeatureOrFilter(LogicalSearchQuery query, boolean onlyIds) {
		return query.getFacetFilters().toSolrFilterQueries().isEmpty()
				&& query.getFieldBoosts().isEmpty()
				&& query.getQueryBoosts().isEmpty()
				&& query.getStartRow() == 0
				&& query.getNumberOfRows() == 100000
				&& query.getStatisticFields().isEmpty()
				&& !query.isPreferAnalyzedFields()
				&& query.getResultsProjection() == null
				&& query.getFieldFacets().isEmpty()
				&& query.getQueryFacets().isEmpty()
				&& (query.getReturnedMetadatas().isFullyLoaded() || onlyIds)
				&& query.getUserFilter() == null
				&& !query.isHighlighting();
	}

}
