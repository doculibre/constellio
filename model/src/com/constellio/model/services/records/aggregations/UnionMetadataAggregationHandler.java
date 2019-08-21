package com.constellio.model.services.records.aggregations;

import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnionMetadataAggregationHandler implements MetadataAggregationHandler {


	@Override
	public Object calculate(SearchAggregatedValuesParams params) {
		//		Set<Object> allValues = new HashSet<>();
		//		for (SearchAggregatedValuesParamsQuery entry : params.getQueries()) {
		//			entry.getStreamSupplier().get().forEach((r) -> {
		//				for (Metadata metadata : entry.getMetadatas()) {
		//					allValues.addAll(r.getValues(metadata));
		//				}
		//			});
		//		}

		LogicalSearchQuery query = new LogicalSearchQuery(params.getCombinedQuery());

		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(params.getInputMetadatas()));
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_CACHE);
		query.setName("RecordReindexing:BackgroundThread:UnionMetadataAggregationHandlerQuery");
		SearchResponseIterator<Record> iterator = params.getSearchServices().recordsIterator(query, 10000);

		Set<Comparable> allValues = new HashSet<>();

		while (iterator.hasNext()) {
			Record record = iterator.next();
			for (Metadata inputMetadata : params.getInputMetadatas()) {
				allValues.addAll((List) record.getValues(inputMetadata));
			}

		}

		List<Comparable> listValues = new ArrayList(allValues);
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
