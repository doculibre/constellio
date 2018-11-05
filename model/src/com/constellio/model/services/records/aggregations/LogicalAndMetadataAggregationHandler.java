package com.constellio.model.services.records.aggregations;

import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.List;

public class LogicalAndMetadataAggregationHandler implements MetadataAggregationHandler {

	@Override
	public Object calculate(SearchAggregatedValuesParams params) {
		List<Metadata> inputMetadatas = params.getInputMetadatas();
		LogicalSearchQuery query = new LogicalSearchQuery(params.getQuery());
		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(inputMetadatas));
		SearchResponseIterator<Record> iterator = params.getSearchServices().recordsIterator(query, 10000);

		while (iterator.hasNext()) {
			Record record = iterator.next();
			for (Metadata inputMetadata : inputMetadatas) {
				for (Boolean value : record.<Boolean>getValues(inputMetadata)) {
					if (Boolean.FALSE.equals(value)) {
						return false;
					}
				}
			}

		}
		return true;
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {
		for (Boolean value : params.<Boolean>getValues()) {
			if (Boolean.FALSE.equals(value)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params) {
		if (params.getCurrentReferenceMetadata() == null) {
			return params.getInputMetadatas();
		}
		return params.getInputMetadatas(params.getCurrentReferenceMetadata());
	}
}
