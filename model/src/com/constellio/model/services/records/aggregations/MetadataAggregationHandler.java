package com.constellio.model.services.records.aggregations;

import java.util.List;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;

public interface MetadataAggregationHandler {

	Object calculate(SearchAggregatedValuesParams params);

	Object calculate(InMemoryAggregatedValuesParams params);

	List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params);

}
