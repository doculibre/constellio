package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams.SearchAggregatedValuesParamsQuery;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class LogicalOrMetadataAggregationHandler implements MetadataAggregationHandler {

	@Override
	public Object calculate(SearchAggregatedValuesParams params) {
		//List<DataStoreField> dataStoreFields = new ArrayList<DataStoreField>(params.getInputMetadatas());

		AtomicBoolean returnedValue = new AtomicBoolean();
		for (SearchAggregatedValuesParamsQuery query : params.getQueries()) {
			Stream<Record> stream = query.getStreamSupplier().get();
			try {
				stream.forEach((r) -> {
					for (Metadata metadata : query.getMetadatas()) {
						for (Boolean booleanValue : r.<Boolean>getValues(metadata)) {
							if (booleanValue) {
								returnedValue.set(true);
							}
						}
					}
				});
			} finally {
				stream.close();
			}
		}
		return returnedValue.get();
		//
		//		LogicalSearchQuery query = new LogicalSearchQuery(params.getCombinedQuery());
		//		query.setCondition(query.getCondition().andWhereAny(dataStoreFields).isTrue());
		//
		//		return params.getSearchServices().hasResults(query);
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {
		for (Boolean value : params.<Boolean>getValues()) {
			if (Boolean.TRUE.equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params) {
		if (params.getCurrentReferenceMetadata() == null) {
			return params.getInputMetadatas();
		}
		return params.getInputMetadatas(params.getCurrentReferenceMetadata());
	}
}
