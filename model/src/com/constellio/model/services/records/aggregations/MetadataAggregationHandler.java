package com.constellio.model.services.records.aggregations;

import java.util.List;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.AggregatedValuesParams;

public interface MetadataAggregationHandler {

	Object calculate(AggregatedValuesParams params);

	List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params);

}
