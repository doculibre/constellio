package com.constellio.model.services.records.aggregations;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.AggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.TransactionAggregatedValuesParams;

public class ReferenceCountMetadataAggregationHandler implements MetadataAggregationHandler {

	@Override
	public Object calculate(AggregatedValuesParams params) {
		return new Double(params.getSearchServices().getResultsCount(params.getQuery()));
	}

	@Override
	public Object calculate(TransactionAggregatedValuesParams params) {
		//TODO
		return 0.0;
	}

	@Override
	public List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params) {
		return new ArrayList<>();
	}

}
