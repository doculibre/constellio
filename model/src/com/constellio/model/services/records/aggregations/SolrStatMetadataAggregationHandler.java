package com.constellio.model.services.records.aggregations;

import java.util.List;
import java.util.Map;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.AggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.TransactionAggregatedValuesParams;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public abstract class SolrStatMetadataAggregationHandler implements MetadataAggregationHandler {

	protected String statName;

	public SolrStatMetadataAggregationHandler(String statName) {
		this.statName = statName;
	}

	@Override
	public Object calculate(AggregatedValuesParams params) {
		Metadata inputMetadata = params.getTypes().getMetadata(params.getAggregatedDataEntry().getFirstInputMetadata());
		LogicalSearchQuery query = params.getQuery();
		query.computeStatsOnField(inputMetadata);
		query.setNumberOfRows(0);
		SPEQueryResponse response = params.getSearchServices().query(query);

		Map<String, Object> statsValues = response.getStatValues(inputMetadata);
		Double value = statsValues == null || statsValues.get(statName) == null ? 0.0 : (Double) statsValues.get(statName);
		return value;
	}

	@Override
	public Object calculate(TransactionAggregatedValuesParams params) {
		//TODO
		return 0.0;
	}

	@Override
	public List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params) {
		return params.getInputMetadatas();
	}

}
