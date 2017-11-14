package com.constellio.model.services.records.aggregations;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.AggregatedValuesParams;

public class ReferenceCountMetadataAggregationHandler implements MetadataAggregationHandler {

	@Override
	public Object calculate(AggregatedValuesParams params) {
		return new Double(params.getSearchServices().getResultsCount(params.getQuery()));
	}

	@Override
	public List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params) {
		return new ArrayList<>();
	}

}
