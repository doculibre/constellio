package com.constellio.model.services.records.aggregations;

import java.util.List;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.AggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.TransactionAggregatedValuesParams;

public interface MetadataAggregationHandler {

	Object calculate(AggregatedValuesParams params);

	Object calculate(TransactionAggregatedValuesParams params);

	List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params);

}
