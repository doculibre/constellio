package com.constellio.model.services.records.aggregations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.AggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.ReindexingAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.TransactionAggregatedValuesParams;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class UnionMetadataAggregationHandler implements MetadataAggregationWithFastReindexingHandler {

	@Override
	public Object calculate(AggregatedValuesParams params) {
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
	public Object calculate(TransactionAggregatedValuesParams params) {
		//TODO
		return new ArrayList<>();
	}

	@Override
	public Object calculate(ReindexingAggregatedValuesParams params) {

		return null;
	}
}
