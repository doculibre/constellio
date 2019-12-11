package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams.SearchAggregatedValuesParamsQuery;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnionMetadataAggregationHandler implements MetadataAggregationHandler {

	@Override
	public Object calculate(SearchAggregatedValuesParams params) {
		Set<Comparable> allValues = new HashSet<>();

		for (SearchAggregatedValuesParamsQuery searchQuery : params.getQueries()) {
			LogicalSearchQuery query = new LogicalSearchQuery(searchQuery.getQuery());
			boolean areAllMetadatasInSummary = params.getInputMetadatas().stream().allMatch(SchemaUtils::isSummary);
			query.setReturnedMetadatas(areAllMetadatasInSummary ?
									   ReturnedMetadatasFilter.onlySummaryFields() :
									   ReturnedMetadatasFilter.onlyMetadatas(params.getInputMetadatas()));

			params.getSearchServices().stream(query, 10000).forEach(record -> {
				for (Metadata inputMetadata : params.getInputMetadatas()) {
					List<String> values = record.getValues(inputMetadata);
					for (Comparable value : values) {
						if (value != null) {
							allValues.add(value);
						}
					}
				}
			});
		}

		List<Comparable> listValues = new ArrayList<>();
		for (Comparable value : allValues) {
			listValues.add(value);
		}
		Collections.sort(listValues);

		return listValues;
	}

	@Override
	public List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params) {
		return params.getInputMetadatas();
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {
		Set<Object> values = new HashSet<>(params.getValues());
		List<Comparable> listValues = new ArrayList(values);
		Collections.sort(listValues);

		return listValues;
	}
}
