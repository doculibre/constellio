package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;

import java.util.List;

public interface MetadataAggregationHandler {

	Object calculate(SearchAggregatedValuesParams params);

	Object calculate(InMemoryAggregatedValuesParams params);

	List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params);

}
