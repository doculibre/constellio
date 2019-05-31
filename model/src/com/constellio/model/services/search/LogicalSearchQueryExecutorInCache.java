package com.constellio.model.services.search;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.List;
import java.util.stream.Stream;

public class LogicalSearchQueryExecutorInCache {

	SearchServices searchServices;
	RecordsCaches recordsCaches;
	MetadataSchemasManager schemasManager;

	public LogicalSearchQueryExecutorInCache(SearchServices searchServices,
											 RecordsCaches recordsCaches,
											 MetadataSchemasManager schemasManager) {
		this.searchServices = searchServices;
		this.recordsCaches = recordsCaches;
		this.schemasManager = schemasManager;
	}

	public Stream<Record> stream(LogicalSearchQuery query) {
		MetadataSchemaType schemaType = getQueriedSchemaType(query.getCondition());
		return recordsCaches.stream(schemaType).filter(query.getCondition());
	}

	public Stream<Record> stream(LogicalSearchCondition condition) {
		return stream(new LogicalSearchQuery(condition));
	}

	public boolean isQueryExecutableInCache(LogicalSearchQuery query) {
		if (!isConditionExecutableInCache(query.getCondition())) {
			return false;
		}

		return hasNoUnsupportedFeatureOrFilter(query);
	}

	public static boolean hasNoUnsupportedFeatureOrFilter(LogicalSearchQuery query) {
		return query.getFacetFilters().toSolrFilterQueries().isEmpty()
			   && query.getFreeTextQuery() == null
			   && query.getSortFields().isEmpty()
			   && query.getFieldPivotFacets().isEmpty()
			   && query.getFieldPivotFacets().isEmpty()
			   && query.getFieldBoosts().isEmpty()
			   && query.getQueryBoosts().isEmpty()
			   && query.getStartRow() == 0
			   && query.getNumberOfRows() == 100000
			   && query.getStatisticFields().isEmpty()
			   && !query.isPreferAnalyzedFields()
			   && query.getResultsProjection() == null
			   && query.getFieldFacets().isEmpty()
			   && query.getFieldPivotFacets().isEmpty()
			   && query.getQueryFacets().isEmpty()
			   && (query.getUserFilters() == null || query.getUserFilters().isEmpty())
			   && !query.isHighlighting();
	}

	public boolean isConditionExecutableInCache(LogicalSearchCondition condition) {

		MetadataSchemaType schemaType = getQueriedSchemaType(condition);
		return schemaType != null && schemaType.getCacheType().hasPermanentCache()
			   && Toggle.USE_CACHE_FOR_QUERY_EXECUTION.isEnabled() && condition.isSupportingMemoryExecution();

	}

	private MetadataSchemaType getQueriedSchemaType(LogicalSearchCondition condition) {
		List<String> schemaTypes = condition.getFilterSchemaTypesCodes();

		if (schemaTypes.size() == 1 && condition.getCollection() != null) {

			MetadataSchemaType schemaType = schemasManager.getSchemaTypes(condition.getCollection())
					.getSchemaType(schemaTypes.get(0));

			return schemaType;

		} else {
			return null;
		}
	}
}
