package com.constellio.model.entities.schemas.entries;

import java.io.Serializable;
import java.util.List;

public interface AggregatedCalculator<T> extends Serializable {
	public T calculate(AggregatedValuesParams params);

	public T calculate(TransactionAggregatedValuesParams params);

	public List<String> getMetadataDependencies();
}
