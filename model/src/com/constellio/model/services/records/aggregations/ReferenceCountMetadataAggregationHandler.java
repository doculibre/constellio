package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;

import java.util.List;

public class ReferenceCountMetadataAggregationHandler implements MetadataAggregationHandler {

	@Override
	public Object calculate(SearchAggregatedValuesParams params) {
		return new Double(params.getSearchServices().getResultsCount(params.getCombinedQuery()));
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {
		return (double) params.getReferenceCount();
	}

	@Override
	public List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params) {
		return params.getReferenceMetadatas();
	}

}
