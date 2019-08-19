package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.ArrayList;
import java.util.List;

public class LogicalOrMetadataAggregationHandler implements MetadataAggregationHandler {

	@Override
	public Object calculate(SearchAggregatedValuesParams params) {
		List<DataStoreField> dataStoreFields = new ArrayList<DataStoreField>(params.getInputMetadatas());

		LogicalSearchQuery query = new LogicalSearchQuery(params.getQuery());
		query.setName("LogicalOrMetadataAggregationHandler:BackgroundThread:Logical OR metadata '" + params.getMetadata().getCode() + "' recalculate for record " + params.getRecord().getId());
		query.setCondition(query.getCondition().andWhereAny(dataStoreFields).isTrue());

		return params.getSearchServices().hasResults(query);
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {
		for (Boolean value : params.<Boolean>getValues()) {
			if (Boolean.TRUE.equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params) {
		if (params.getCurrentReferenceMetadata() == null) {
			return params.getInputMetadatas();
		}
		return params.getInputMetadatas(params.getCurrentReferenceMetadata());
	}
}
