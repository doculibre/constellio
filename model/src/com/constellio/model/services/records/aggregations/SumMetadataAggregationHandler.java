package com.constellio.model.services.records.aggregations;

import java.util.Set;

import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;

public class SumMetadataAggregationHandler extends SolrStatMetadataAggregationHandler {

	public SumMetadataAggregationHandler() {
		super("sum");
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {

		double sum = 0.0;

		for (Object value : params.getValues()) {
			if (value instanceof Number) {
				sum += ((Number) value).doubleValue();
			}
		}

		return sum;
	}

	@Override
	protected Object calculateForNonNumber(MetadataValueType metadataValueType, Set<Object> values) {

		//Not working with other types
		return null;
	}
}
