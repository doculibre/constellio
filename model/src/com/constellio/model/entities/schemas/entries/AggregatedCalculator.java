package com.constellio.model.entities.schemas.entries;

import java.io.Serializable;
import java.util.List;

public interface AggregatedCalculator<T> extends Serializable {
	T calculate(SearchAggregatedValuesParams params);

	T calculate(InMemoryAggregatedValuesParams params);

	List<String> getMetadataDependencies();
}
