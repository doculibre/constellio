package com.constellio.model.services.records.aggregations;

import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.*;

public class UnionMetadataAggregationHandler implements MetadataAggregationHandler {

	@Override
	public Object calculate(SearchAggregatedValuesParams params) {
		List<Metadata> inputMetadatas = params.getInputMetadatas();
		LogicalSearchQuery query = new LogicalSearchQuery(params.getQuery());
		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(inputMetadatas));
		SearchResponseIterator<Record> iterator = params.getSearchServices().recordsIterator(query, 10000);

		Set<Comparable> values = new HashSet<>();

		while (iterator.hasNext()) {
			Record record = iterator.next();
			for (Metadata inputMetadata : inputMetadatas) {
				values.addAll((List) record.getValues(inputMetadata));
			}

		}

		List<Comparable> listValues = new ArrayList<>(values);
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
